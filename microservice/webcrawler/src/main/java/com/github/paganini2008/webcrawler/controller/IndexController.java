package com.github.paganini2008.webcrawler.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.paganini2008.devtools.multithreads.ThreadUtils;
import com.github.paganini2008.springdessert.webcrawler.es.IndexedResourceService;
import com.github.paganini2008.webcrawler.utils.Response;

/**
 * 
 * IndexController
 *
 * @author Fred Feng
 * 
 * @since 1.0
 */
@RequestMapping("/index")
@RestController
public class IndexController {

	@Autowired
	private IndexedResourceService indexedResourceService;

	@GetMapping("/catalog/{id}/index")
	public Response indexAll(@PathVariable("id") Long catalogId) {
		ThreadUtils.runAsThread(() -> {
			indexedResourceService.indexAll(catalogId, false);
		});
		return Response.success("Submit OK.");
	}
	
}
