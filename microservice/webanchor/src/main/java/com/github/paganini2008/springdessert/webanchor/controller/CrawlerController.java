package com.github.paganini2008.springdessert.webanchor.controller;

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
import com.github.paganini2008.springdessert.webanchor.utils.PageBean;
import com.github.paganini2008.springdessert.webanchor.utils.Response;
import com.github.paganini2008.springdessert.webcrawler.CrawlerLauncher;
import com.github.paganini2008.springdessert.webcrawler.PageExtractor;
import com.github.paganini2008.springdessert.webcrawler.ResourceManager;
import com.github.paganini2008.springdessert.webcrawler.model.Catalog;

/**
 * 
 * CrawlerController
 *
 * @author Fred Feng
 * @since 1.0
 */
@RequestMapping("/crawler")
@RestController
public class CrawlerController {

	@Autowired
	private CrawlerLauncher crawlerLauncher;

	@Autowired
	private ResourceManager resourceManager;

	@Autowired
	private PageExtractor pageExtractor;

	@GetMapping("/catalog/{id}/delete")
	public Response deleteCatalog(@PathVariable("id") Long catalogId) {
		resourceManager.deleteCatalog(catalogId);
		return Response.success("Delete OK.");
	}

	@GetMapping("/catalog/{id}/crawl")
	public Response crawl(@PathVariable("id") Long catalogId) {
		crawlerLauncher.submit(catalogId);
		return Response.success("Job will be triggered soon.");
	}

	@PostMapping("/catalog/save")
	public Response saveSource(@RequestBody Catalog catalog) {
		resourceManager.saveCatalog(catalog);
		return Response.success("Save OK.");
	}

	@GetMapping("/catalog")
	public Response queryForCatalog(@RequestParam(value = "page", defaultValue = "1", required = false) int page,
			@CookieValue(value = "DATA_LIST_SIZE", required = false, defaultValue = "10") int size) {
		PageResponse<Catalog> pageResponse = resourceManager.queryForCatalog(page, size);
		return Response.success(PageBean.wrap(pageResponse));
	}

	@GetMapping("/fetch")
	public String testRequest(@RequestParam("url") String url) throws Exception {
		return pageExtractor.extractHtml(url);
	}

}
