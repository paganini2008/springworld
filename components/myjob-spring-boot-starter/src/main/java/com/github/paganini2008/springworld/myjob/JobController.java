package com.github.paganini2008.springworld.myjob;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 
 * JobController
 * @author Fred Feng
 *
 * @since 1.0
 */
@RestController
@RequestMapping("/job/admin")
public class JobController {

	@Autowired
	private JobAdmin jobAdmin;
	
	@PostMapping("/triggerJob")
	public ResponseEntity<JobResult> triggerJob(@RequestBody JobParam jobParam) {
		JobState jobState = jobAdmin.triggerJob(jobParam.getJobKey(), jobParam.getAttachment());
		return ResponseEntity.ok(JobResult.success(jobState, "ok"));
	}
	
}
