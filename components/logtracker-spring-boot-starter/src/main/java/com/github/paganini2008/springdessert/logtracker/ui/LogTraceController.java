package com.github.paganini2008.springdessert.logtracker.ui;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.paganini2008.devtools.jdbc.PageResponse;
import com.github.paganini2008.springdessert.logtracker.es.LogEntryService;

/**
 * 
 * LogTraceController
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
@RequestMapping("/application/cluster/log")
@RestController
public class LogTraceController {

	@Autowired
	private LogEntryService logEntryService;

	@GetMapping("/")
	public Response search(@RequestParam(value = "page", defaultValue = "1", required = false) int page,
			@CookieValue(value = "DATA_LIST_SIZE", required = false, defaultValue = "100") int size) {
		PageResponse<SearchResult> pageResponse = logEntryService.search(page, size);
		return Response.success(PageBean.wrap(pageResponse));
	}

	@PostMapping("/search")
	public Response search(@RequestBody SearchQuery searchQuery,
			@RequestParam(value = "page", defaultValue = "1", required = false) int page,
			@CookieValue(value = "DATA_LIST_SIZE", required = false, defaultValue = "100") int size) {
		PageResponse<SearchResult> pageResponse = logEntryService.search(searchQuery, page, size);
		return Response.success(PageBean.wrap(pageResponse));
	}

	@GetMapping("/search")
	public Response search(@RequestParam("q") String keyword, @RequestParam(value = "page", defaultValue = "1", required = false) int page,
			@CookieValue(value = "DATA_LIST_SIZE", required = false, defaultValue = "100") int size) {
		PageResponse<SearchResult> pageResponse = logEntryService.search(keyword, page, size);
		return Response.success(PageBean.wrap(pageResponse));
	}

}
