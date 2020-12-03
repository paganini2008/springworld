package com.github.paganini2008.webcrawler.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.paganini2008.devtools.jdbc.PageResponse;
import com.github.paganini2008.springdessert.webcrawler.PageExtractor;
import com.github.paganini2008.springdessert.webcrawler.es.IndexedResourceService;
import com.github.paganini2008.springdessert.webcrawler.es.SearchResult;
import com.github.paganini2008.webcrawler.utils.PageBean;
import com.github.paganini2008.webcrawler.utils.Response;

/**
 * 
 * SearchController
 *
 * @author Jimmy Hoff
 * @since 1.0
 */
@RequestMapping("/search")
@RestController
public class SearchController {

	@Autowired
	private PageExtractor pageExtractor;

	@Autowired
	private IndexedResourceService indexedResourceService;

	@GetMapping("/fetch")
	public String testRequest(@RequestParam("url") String url) throws Exception {
		return pageExtractor.extractHtml("", url);
	}

	@GetMapping("/search")
	public Response search(@RequestParam("q") String keyword, @RequestParam(value = "page", defaultValue = "1", required = false) int page,
			@CookieValue(value = "DATA_LIST_SIZE", required = false, defaultValue = "10") int size) {
		PageResponse<SearchResult> pageResponse = indexedResourceService.search(keyword, page, size);
		return Response.success(PageBean.wrap(pageResponse));
	}

}
