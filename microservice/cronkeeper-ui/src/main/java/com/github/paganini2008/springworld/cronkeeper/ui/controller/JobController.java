package com.github.paganini2008.springworld.cronkeeper.ui.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.github.paganini2008.springworld.cronkeeper.model.JobDetail;
import com.github.paganini2008.springworld.cronkeeper.model.JobTrace;
import com.github.paganini2008.springworld.cronkeeper.model.PageQuery;
import com.github.paganini2008.springworld.cronkeeper.ui.JobTraceForm;
import com.github.paganini2008.springworld.cronkeeper.ui.service.JobManagerService;
import com.github.paganini2008.springworld.cronkeeper.ui.utils.PageBean;

/**
 * 
 * JobController
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@RequestMapping("/job")
@Controller
public class JobController {

	@Autowired
	private JobManagerService jobManagerService;

	@PostMapping("")
	public String selectJobDetail(@RequestParam("clusterName") String clusterName,
			@RequestParam(value = "page", defaultValue = "1", required = false) int page,
			@CookieValue(value = "DATA_LIST_SIZE", required = false, defaultValue = "10") int size, Model ui) throws Exception {
		PageQuery<JobDetail> pageQuery = jobManagerService.selectJobDetail(clusterName, page, size);
		ui.addAttribute("page", PageBean.wrap(pageQuery));
		return "job_list";
	}

	@PostMapping("/trace/select")
	public String selectJobTrace(@ModelAttribute JobTraceForm form,
			@RequestParam(value = "page", defaultValue = "1", required = false) int page,
			@CookieValue(value = "DATA_LIST_SIZE", required = false, defaultValue = "10") int size, Model ui) throws Exception {
		PageQuery<JobTrace> pageQuery = jobManagerService.selectJobTrace(form, page, size);
		ui.addAttribute("page", PageBean.wrap(pageQuery));
		return "select_job_trace";
	}

	@GetMapping("/detail/{jobKey}")
	public String selectJobDetail(@PathVariable("jobKey") String jobKey, Model ui) throws Exception {
		JobDetail jobDetail = jobManagerService.getJobDetail(jobKey);
		ui.addAttribute("jobDetail", jobDetail);
		return "job_detail";
	}

}
