package com.github.paganini2008.springworld.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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

	@Qualifier("main-job-executor")
	@Autowired
	private JobExecutor jobExecutor;

	@Qualifier("internal-job-bean-loader")
	@Autowired
	private JobBeanLoader jobBeanLoader;

	@Qualifier("external-job-bean-loader")
	@Autowired(required = false)
	private JobBeanLoader externalJobBeanLoader;

	@Autowired
	private JobManager jobManager;

	@PostMapping("/run")
	public ResponseEntity<JobResult> runJob(@RequestBody JobParam jobParam) throws Exception {
		final JobKey jobKey = jobParam.getJobKey();
		Job job = jobBeanLoader.loadJobBean(jobKey);
		if (job == null && externalJobBeanLoader != null) {
			job = externalJobBeanLoader.loadJobBean(jobKey);
		}
		jobExecutor.execute(job, jobParam.getAttachment());
		JobResult jobResult = JobResult.success(jobManager.getJobRuntime(jobParam.getJobKey()).getJobState(), "ok");
		return ResponseEntity.ok(jobResult);
	}

}
