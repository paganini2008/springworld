package com.github.paganini2008.springdessert.logtracker.ui;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 
 * PageController
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
@RequestMapping("/application/cluster/log")
@Controller
public class PageController {

	@GetMapping("/realtime")
	public String realtime(Model ui) {
		return "realtime";
	}

	@GetMapping("/query")
	public String query(Model ui) {
		return "query";
	}

}
