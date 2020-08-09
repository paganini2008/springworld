package com.github.paganini2008.springworld.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 
 * JobController
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@RestController
@RequestMapping("/job")
public class JobController {

	@Autowired
	private JobExecutor jobExecutor;

	@Autowired
	private JobBeanLoader jobBeanLoader;

	@Autowired
	private JobManager jobManager;

	@PostMapping("/run")
	public ResponseEntity<JobResult> runJob(@RequestBody JobParam jobParam) throws Exception {
		Job job = jobBeanLoader.loadJobBean(jobParam.getJobKey());
		jobExecutor.execute(job, jobParam.getAttachment());
		JobResult jobResult = JobResult.success(jobManager.getJobRuntime(job).getJobState(), "ok");
		return ResponseEntity.ok(jobResult);
	}

}
