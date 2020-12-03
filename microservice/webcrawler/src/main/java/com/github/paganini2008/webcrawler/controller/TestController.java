package com.github.paganini2008.webcrawler.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.paganini2008.springdessert.webcrawler.PathFilter;
import com.github.paganini2008.springdessert.webcrawler.PathFilterFactory;
import com.github.paganini2008.springdessert.webcrawler.model.Catalog;
import com.github.paganini2008.xtransport.NioClient;
import com.github.paganini2008.xtransport.Partitioner;
import com.github.paganini2008.xtransport.Tuple;

/**
 * 
 * TestController
 *
 * @author Jimmy Hoff
 * 
 * @since 1.0
 */
@RestController
public class TestController {

	@Autowired
	private NioClient nioClient;

	@Autowired
	private Partitioner partitioner;
	
	@Autowired
	private PathFilterFactory pathFilterFactory;
	
	@GetMapping("/testExists")
	public Map<String, Object> testExists(@RequestParam("q") String q){
		PathFilter pathFilter = pathFilterFactory.getPathFilter("test");
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("exists", pathFilter.mightExist(q));
		return data;
	}

	@GetMapping("/index")
	public Map<String, Object> echo() {
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("data", "hello");
		return data;
	}

	@PostMapping("/testJson")
	public Map<String, Object> testJson(@RequestBody(required = false) Catalog source) {
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("data", source.toString());
		return data;
	}

	@GetMapping("/test/socket")
	public Map<String, Object> testSocket(@RequestParam("q") String content) {
		Map<String, Object> result = new HashMap<String, Object>();
		nioClient.send(Tuple.byString(content), partitioner);
		result.put("success", true);
		return result;
	}

}
