package com.github.paganini2008.springworld.cronkeeper.ui.controller.bak;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.paganini2008.springworld.cronkeeper.ui.UIModel;
import com.github.paganini2008.springworld.cronkeeper.ui.model.JobDetail;
import com.github.paganini2008.springworld.cronkeeper.ui.model.JobInfo;
import com.github.paganini2008.springworld.cronkeeper.ui.model.JobTrace;
import com.github.paganini2008.springworld.cronkeeper.ui.model.Page;
import com.github.paganini2008.springworld.cronkeeper.ui.service.JobManagerService;

/**
 * 
 * JobController
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
//@RequestMapping("/job")
//@RestController
public class JobController {

	@Autowired
	private JobManagerService jobService;

	@PostMapping("/info/select")
	public UIModel<Page<JobInfo>> selectJobInfo(@RequestParam("draw") int draw, @RequestParam("start") int start,
			@RequestParam("length") int length) {
		Page<JobInfo> result = jobService.selectJobInfo(draw, (start / length) + 1, length);
		return UIModel.success(result);
	}

	@PostMapping("/trace/select")
	public UIModel<Page<JobTrace>> selectJobTrace(@RequestParam("draw") int draw, @RequestParam("jobId") int jobId,
			@RequestParam("start") int start, @RequestParam("length") int length) {
		Page<JobTrace> result = jobService.selectJobTrace(draw, jobId, (start / length) + 1, length);
		return UIModel.success(result);
	}

	@GetMapping("/detail/{jobId}")
	public UIModel<JobDetail> selectJobDetail(@PathVariable("jobId") int jobId) {
		JobDetail jobDetail = jobService.selectJobDetail(jobId);
		return UIModel.success(jobDetail);
	}

}
