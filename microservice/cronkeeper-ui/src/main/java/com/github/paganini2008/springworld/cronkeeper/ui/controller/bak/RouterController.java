package com.github.paganini2008.springworld.cronkeeper.ui.controller.bak;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 
 * RouterController
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
//@Controller
//@RequestMapping("/job")
public class RouterController {

	@GetMapping("/console")
	public String index(Model ui) throws Exception {
		return "index";
	}

	@GetMapping("/detail/{jobId}")
	public String detail(@PathVariable("jobId") int jobId, Model ui) throws Exception {
		ui.addAttribute("jobId", jobId);
		return "detail";
	}

}
