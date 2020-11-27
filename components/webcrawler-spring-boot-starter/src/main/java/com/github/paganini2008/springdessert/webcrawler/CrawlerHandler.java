package com.github.paganini2008.springdessert.webcrawler;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.github.paganini2008.devtools.StringUtils;
import com.github.paganini2008.devtools.collection.CollectionUtils;
import com.github.paganini2008.devtools.collection.MapUtils;
import com.github.paganini2008.springdessert.transport.Handler;
import com.github.paganini2008.springdessert.webcrawler.index.IndexedResourceService;
import com.github.paganini2008.springdessert.webcrawler.model.Catalog;
import com.github.paganini2008.springdessert.webcrawler.model.Resource;
import com.github.paganini2008.transport.NioClient;
import com.github.paganini2008.transport.Partitioner;
import com.github.paganini2008.transport.Tuple;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * CrawlerHandler
 *
 * @author Fred Feng
 * @since 1.0
 */
@Slf4j
@Component
public class CrawlerHandler implements Handler {

	private static final String UNIQUE_PATH_IDENTIFIER = "%s$%s$%s$%s";

	@Autowired
	private PageExtractor pageExtractor;

	@Autowired
	private ResourceManager resourceManager;

	@Autowired
	private NioClient nioClient;

	@Autowired
	private Partitioner partitioner;

	@Qualifier("secondaryPartitioner")
	@Autowired
	private Partitioner secondaryPartitioner;

	@Autowired
	private PathAcceptor pathAcceptor;

	@Autowired
	private FinishableCondition finishCondition;

	@Autowired
	private PathFilterFactory pathFilterFactory;

	@Autowired
	private IndexedResourceService indexService;

	@Value("${spring.application.name}")
	private String applicationName;

	@Value("${webcrawler.crawler.fetch.depth:-1}")
	private int depth;

	@Value("${webcrawler.indexer.enabled:false}")
	private boolean indexEnabled;

	private final Map<Long, Catalog> catalogCache = new ConcurrentHashMap<Long, Catalog>();
	private final Map<Long, PathFilter> pathFilters = new ConcurrentHashMap<Long, PathFilter>();

	private PathFilter getPathFilter(long catalogId) {
		return MapUtils.get(pathFilters, catalogId, () -> {
			String identifier = applicationName + ":" + catalogId;
			return pathFilterFactory.getPathFilter(identifier);
		});
	}

	public void onData(Tuple tuple) {
		final String action = (String) tuple.getField("action");
		if (StringUtils.isNotBlank(action)) {
			switch (action) {
			case "crawl":
				doCrawl(tuple);
				break;
			case "index":
				doIndex(tuple);
				break;
			}
		}
	}

	private void doCrawl(Tuple tuple) {
		if (finishCondition.couldFinish(tuple)) {
			return;
		}
		final long catalogId = (Long) tuple.getField("catalogId");
		final String refer = (String) tuple.getField("refer");
		final String path = (String) tuple.getField("path");
		final String type = (String) tuple.getField("type");
		final int version = (Integer) tuple.getField("version");

		PathFilter pathFilter = getPathFilter(catalogId);
		String pathIdentifier = String.format(UNIQUE_PATH_IDENTIFIER, catalogId, refer, path, version);
		if (pathFilter.mightExist(pathIdentifier)) {
			return;
		}
		String html = null;
		try {
			html = pageExtractor.extractHtml(path);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		if (StringUtils.isBlank(html)) {
			return;
		}
		Document document = Jsoup.parse(html);
		if (document == null) {
			return;
		}

		Resource resource = new Resource();
		resource.setTitle(document.title());
		resource.setHtml(document.html());
		resource.setUrl(path);
		resource.setType(type);
		resource.setCreateTime(new Date());
		resource.setVersion(version);
		resource.setCatalogId(catalogId);
		resourceManager.saveResource(resource);
		if (log.isTraceEnabled()) {
			log.trace("Save: " + resource);
		}
		if (indexEnabled) {
			sendIndex(catalogId, resource.getId(), version);
		}
		Elements elements = document.body().select("a");
		if (CollectionUtils.isNotEmpty(elements)) {
			String href;
			for (Element element : elements) {
				href = element.absUrl("href");
				if (StringUtils.isBlank(href)) {
					href = element.attr("href");
					if (!href.startsWith("/")) {
						href = "/" + href;
					}
					href = refer + href;
				}
				if (StringUtils.isNotBlank(href) && acceptedPath(refer, href, tuple)) {
					sendRecursively(catalogId, refer, href, type, version, pathFilter);
				}
			}
		}
	}

	private void doIndex(Tuple tuple) {
		long catalogId = (Long) tuple.getField("catalogId");
		Catalog catalog = MapUtils.get(catalogCache, catalogId, () -> {
			return resourceManager.getCatalog(catalogId);
		});
		long resourceId = (Long) tuple.getField("resourceId");
		int version = (Integer) tuple.getField("version");
		Resource resource = resourceManager.getResource(resourceId);
		indexService.index(catalog, resource, false, version);
		log.info("Index: " + resource.toString());
	}

	private boolean acceptedPath(String refer, String path, Tuple tuple) {
		if (!pathAcceptor.accept(refer, path, tuple)) {
			return false;
		}
		if (!testDepth(refer, path)) {
			return false;
		}
		return true;
	}

	private boolean testDepth(String refer, String path) {
		if (depth < 0) {
			return true;
		}
		String part = path.replace(refer, "");
		if (part.charAt(0) == '/') {
			part = part.substring(1);
		}
		int n = 0;
		for (char ch : part.toCharArray()) {
			if (ch == '/') {
				n++;
			}
		}
		return n <= depth;
	}

	private void sendRecursively(long catalogId, String refer, String path, String type, int version, PathFilter pathFilter) {
		String pathIdentifier = String.format(UNIQUE_PATH_IDENTIFIER, catalogId, refer, path, version);
		if (!pathFilter.mightExist(pathIdentifier)) {
			Tuple tuple = Tuple.newOne();
			tuple.setField("action", "crawl");
			tuple.setField("catalogId", catalogId);
			tuple.setField("refer", refer);
			tuple.setField("path", path);
			tuple.setField("type", type);
			tuple.setField("version", version);
			nioClient.send(tuple, partitioner);
		}
	}

	private void sendIndex(long catalogId, long resourceId, int version) {
		Tuple tuple = Tuple.newOne();
		tuple.setField("action", "index");
		tuple.setField("catalogId", catalogId);
		tuple.setField("resourceId", resourceId);
		tuple.setField("version", version);
		nioClient.send(tuple, secondaryPartitioner);
	}

}
