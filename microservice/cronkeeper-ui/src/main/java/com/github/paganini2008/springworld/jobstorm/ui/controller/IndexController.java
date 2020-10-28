package com.github.paganini2008.springworld.jobstorm.ui.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.WebRequest;

import com.github.paganini2008.devtools.ArrayUtils;
import com.github.paganini2008.devtools.StringUtils;
import com.github.paganini2008.springworld.jobstorm.ui.service.JobManagerService;

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
	public String index(@RequestParam(name = "clusterName", required = false) String clusterName, WebRequest webRequest, Model ui)
			throws Exception {
		String[] clusterNames = jobManagerService.selectClusterNames();
		ui.addAttribute("clusterNames", clusterNames);
		if (StringUtils.isBlank(clusterName) && ArrayUtils.isNotEmpty(clusterNames)) {
			clusterName = clusterNames[0];
		}
		webRequest.setAttribute("currentClusterName", clusterName, RequestAttributes.SCOPE_SESSION);
		return "index";
	}

}
