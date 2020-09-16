package com.github.paganini2008.springworld.cronkeeper.ui.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.github.paganini2008.springworld.cronkeeper.ui.service.JobManagerService;

/**
 * 
 * IndexController
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Controller
public class IndexController {

	@Autowired
	private JobManagerService jobManagerService;

	@GetMapping("/index")
	public String index(Model ui) throws Exception {
		String[] clusterNames = jobManagerService.selectClusterNames();
		ui.addAttribute("clusterNames", clusterNames);
		return "index";
	}

}
