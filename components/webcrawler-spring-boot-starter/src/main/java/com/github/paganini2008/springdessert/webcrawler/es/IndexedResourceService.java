package com.github.paganini2008.springdessert.webcrawler.es;

import static com.github.paganini2008.springdessert.webcrawler.es.SearchResult.SEARCH_FIELD_SOURCE;
import static com.github.paganini2008.springdessert.webcrawler.es.SearchResult.SEARCH_FIELD_VERSION;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.DeleteQuery;
import org.springframework.util.StopWatch;

import com.github.paganini2008.devtools.date.Duration;
import com.github.paganini2008.devtools.jdbc.PageRequest;
import com.github.paganini2008.devtools.jdbc.PageResponse;
import com.github.paganini2008.devtools.jdbc.ResultSetSlice;
import com.github.paganini2008.springdessert.webcrawler.PageExtractor;
import com.github.paganini2008.springdessert.webcrawler.ResourceManager;
import com.github.paganini2008.springdessert.webcrawler.model.Catalog;
import com.github.paganini2008.springdessert.webcrawler.model.CatalogIndex;
import com.github.paganini2008.springdessert.webcrawler.model.Resource;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * IndexedResourceService
 *
 * @author Fred Feng
 * @since 1.0
 */
@Slf4j
public class IndexedResourceService {

	@Autowired
	private IndexedResourceRepository indexedResourceRepository;

	@Autowired
	private ResourceManager resourceManager;

	@Autowired
	private PageExtractor pageExtractor;

	@Autowired
	private ElasticsearchTemplate elasticsearchTemplate;

	public void createIndex(String indexName) {
		elasticsearchTemplate.createIndex(indexName);
	}

	public void deleteIndex(String indexName) {
		elasticsearchTemplate.deleteIndex(indexName);
	}

	public void deleteResource(long catalogId, int version) {
		Catalog catalog = resourceManager.getCatalog(catalogId);
		BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery()
				.should(QueryBuilders.termQuery(SEARCH_FIELD_SOURCE, catalog.getName()));
		if (version > 0) {
			boolQueryBuilder = boolQueryBuilder.filter(QueryBuilders.termQuery(SEARCH_FIELD_VERSION, version));
		}
		DeleteQuery deleteQuery = new DeleteQuery();
		deleteQuery.setQuery(boolQueryBuilder);
		elasticsearchTemplate.delete(deleteQuery);
	}

	public void saveResource(IndexedResource indexedResource) {
		indexedResourceRepository.save(indexedResource);
	}

	public void deleteResource(IndexedResource indexedResource) {
		indexedResourceRepository.delete(indexedResource);
	}

	public long indexCount() {
		return indexedResourceRepository.count();
	}

	public void indexAll(boolean upgrade) {
		StopWatch stopWatch = new StopWatch();
		int page = 1;
		PageResponse<Catalog> pageResponse = resourceManager.queryForCatalog(page, 10);
		for (PageResponse<Catalog> current : pageResponse) {
			for (Catalog catalog : current.getContent()) {
				stopWatch.start(String.format("[%s<%s>]", catalog.getName(), catalog.getUrl()));
				indexAll(catalog.getId(), upgrade);
				stopWatch.stop();
			}
		}
		log.info(stopWatch.prettyPrint());
	}

	public void indexAll(long catalogId, boolean upgrade) {
		long startTime = System.currentTimeMillis();
		Catalog catalog = resourceManager.getCatalog(catalogId);
		log.info("Start to index catalog '{}' ...", catalog.getName());
		if (upgrade) {
			resourceManager.incrementCatalogIndexVersion(catalogId);
		}
		CatalogIndex catalogIndex = resourceManager.getCatalogIndex(catalogId);
		int page = 1;
		PageResponse<Resource> pageResponse = resourceManager.queryForResourceForIndex(catalogId, page, 100);
		for (PageResponse<Resource> current : pageResponse) {
			for (Resource resource : current.getContent()) {
				try {
					index(catalog, resource, true, catalogIndex.getVersion());
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			}
		}
		int effectedRows = resourceManager.updateResourceVersion(catalogId, catalogIndex.getVersion());
		log.info("Index catalog '{}' completedly. Effected rows: {}, Total time: {}", catalog.getName(), effectedRows,
				Duration.HOUR.format(System.currentTimeMillis() - startTime));
	}

	public void index(Catalog catalog, Resource resource, boolean refresh, int version) {
		IndexedResource indexedResource = new IndexedResource();
		String html = resource.getHtml();
		if (refresh) {
			try {
				html = pageExtractor.extractHtml(resource.getUrl());
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
		Document document = Jsoup.parse(html);
		indexedResource.setId(resource.getId());
		indexedResource.setTitle(resource.getTitle());
		indexedResource.setContent(document.body().text());
		indexedResource.setPath(resource.getUrl());
		indexedResource.setType(resource.getType());
		indexedResource.setUrl(catalog.getUrl());
		indexedResource.setCatalog(catalog.getName());
		indexedResource.setCreateTime(resource.getCreateTime().getTime());
		indexedResource.setVersion(version);
		indexedResourceRepository.save(indexedResource);
		if (log.isTraceEnabled()) {
			log.trace("Index resource: " + resource.toString());
		}
	}

	public PageResponse<SearchResult> search(String keyword, int page, int size) {
		return search(keyword).list(PageRequest.of(page, size));
	}

	public ResultSetSlice<SearchResult> search(String keyword) {
		int version = resourceManager.maximumVersionOfCatalogIndex();
		return new ElasticsearchTemplateResultSlice(keyword, version, elasticsearchTemplate);
	}

}
