package com.github.paganini2008.webcrawler.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.paganini2008.devtools.jdbc.PageResponse;
import com.github.paganini2008.springdessert.webcrawler.CrawlerLauncher;
import com.github.paganini2008.springdessert.webcrawler.CrawlerSummary;
import com.github.paganini2008.springdessert.webcrawler.CrawlerSummary.Summary;
import com.github.paganini2008.springdessert.webcrawler.ResourceManager;
import com.github.paganini2008.springdessert.webcrawler.es.IndexedResourceService;
import com.github.paganini2008.springdessert.webcrawler.model.Catalog;
import com.github.paganini2008.webcrawler.utils.PageBean;
import com.github.paganini2008.webcrawler.utils.Response;

/**
 * 
 * CrawlerController
 *
 * @author Jimmy Hoff
 * @since 1.0
 */
@RequestMapping("/catalog")
@RestController
public class CatalogController {

	@Autowired
	private CrawlerLauncher crawlerLauncher;

	@Autowired
	private ResourceManager resourceManager;

	@Autowired
	private IndexedResourceService indexedResourceService;

	@Autowired
	private CrawlerSummary crawlerSummary;

	@GetMapping("/{id}/delete")
	public Response deleteCatalog(@PathVariable("id") Long catalogId,
			@RequestParam(name = "cascade", required = false, defaultValue = "false") boolean cascade) {
		if (cascade) {
			indexedResourceService.deleteResource(catalogId, 0);
			resourceManager.deleteResourceByCatalogId(catalogId);
		}
		resourceManager.deleteCatalog(catalogId);
		return Response.success("Delete OK.");
	}

	@PostMapping("/{id}/clean")
	public Response cleanCatalog(@PathVariable("id") Long catalogId) {
		indexedResourceService.deleteResource(catalogId, 0);
		resourceManager.deleteResourceByCatalogId(catalogId);
		return Response.success("Clean OK.");
	}

	@PostMapping("/{id}/rebuild")
	public Response rebuild(@PathVariable("id") Long catalogId) {
		crawlerLauncher.rebuild(catalogId);
		return Response.success("Crawling Job will be triggered soon.");
	}

	@PostMapping("/{id}/crawl")
	public Response crawl(@PathVariable("id") Long catalogId) {
		crawlerLauncher.submit(catalogId);
		return Response.success("Crawling Job will be triggered soon.");
	}

	@PostMapping("/{id}/update")
	public Response update(@PathVariable("id") Long catalogId) {
		crawlerLauncher.update(catalogId);
		return Response.success("Crawling Job will be triggered soon.");
	}

	@PostMapping("/save")
	public Response saveCatalog(@RequestBody Catalog catalog) {
		resourceManager.saveCatalog(catalog);
		return Response.success("Save OK.");
	}

	@GetMapping("/{id}/summary")
	public Response summary(@PathVariable("id") Long catalogId) {
		Summary summary = crawlerSummary.getSummary(catalogId);
		return Response.success(summary);
	}

	@GetMapping("/query")
	public Response queryForCatalog(@RequestParam(value = "page", defaultValue = "1", required = false) int page,
			@CookieValue(value = "DATA_LIST_SIZE", required = false, defaultValue = "10") int size) {
		PageResponse<Catalog> pageResponse = resourceManager.queryForCatalog(page, size);
		return Response.success(PageBean.wrap(pageResponse));
	}

}
