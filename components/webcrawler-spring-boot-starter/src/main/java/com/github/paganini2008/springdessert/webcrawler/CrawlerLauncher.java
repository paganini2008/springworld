package com.github.paganini2008.springdessert.webcrawler;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.github.paganini2008.springdessert.webcrawler.model.Catalog;
import com.github.paganini2008.springdessert.webcrawler.model.CatalogIndex;
import com.github.paganini2008.xtransport.NioClient;
import com.github.paganini2008.xtransport.Partitioner;
import com.github.paganini2008.xtransport.Tuple;

/**
 * 
 * CrawlerLauncher
 *
 * @author Jimmy Hoff
 * 
 * @since 1.0
 */
public final class CrawlerLauncher {

	@Value("${spring.application.name}")
	private String applicationName;

	@Autowired
	private NioClient nioClient;

	@Autowired
	private Partitioner partitioner;

	@Autowired
	private ResourceManager resourceManager;

	@Autowired
	private PathFilterFactory pathFilterFactory;

	@Autowired
	private CrawlerSummary crawlerSummary;

	@Autowired
	private ConditionalTermination condition;

	@Value("${webcrawler.indexer.enabled:true}")
	private boolean indexEnabled;

	public void rebuild(long catalogId) {
		final String identifier = applicationName + ":" + catalogId;
		pathFilterFactory.clean(identifier);
		submit(catalogId);
	}

	public void submit(long catalogId) {

		crawlerSummary.reset(catalogId);
		condition.reset(catalogId);

		Catalog catalog = resourceManager.getCatalog(catalogId);
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("action", "crawl");
		data.put("catalogId", catalog.getId());
		data.put("refer", catalog.getUrl());
		data.put("path", catalog.getUrl());
		data.put("cat", catalog.getCat());
		data.put("version", indexEnabled ? getIndexVersion(catalogId) : 0);
		nioClient.send(Tuple.wrap(data), partitioner);
	}

	public void update(long catalogId) {

		crawlerSummary.reset(catalogId);
		condition.reset(catalogId);

		Catalog catalog = resourceManager.getCatalog(catalogId);
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("action", "update");
		data.put("catalogId", catalog.getId());
		data.put("refer", catalog.getUrl());
		data.put("path", catalog.getUrl());
		data.put("cat", catalog.getCat());
		data.put("version", indexEnabled ? getIndexVersion(catalogId) : 0);
		nioClient.send(Tuple.wrap(data), partitioner);
	}

	private int getIndexVersion(long catalogId) {
		CatalogIndex catalogIndex = resourceManager.getCatalogIndex(catalogId);
		return catalogIndex.getVersion();
	}
}
