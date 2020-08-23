package com.github.paganini2008.springworld.myjob;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 
 * JobAdminController
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@RestController
@RequestMapping("/job/admin")
public class JobAdminController {

	@Autowired
	private JobAdmin jobAdmin;

	@PostMapping("/persistJob")
	public ResponseEntity<JobResult> persistJob(@RequestBody JobConfig jobConfig) {
		JobState jobState = jobAdmin.persistJob(jobConfig);
		return ResponseEntity.ok(JobResult.success(jobState, "ok"));
	}

	@PostMapping("/hasJob")
	public ResponseEntity<JobResult> hasJob(@RequestBody JobKey jobKey) {
		JobState jobState = jobAdmin.hasJob(jobKey);
		return ResponseEntity.ok(JobResult.success(jobState, "ok"));
	}

	@DeleteMapping("/deleteJob")
	public ResponseEntity<JobResult> deleteJob(@RequestBody JobKey jobKey) {
		JobState jobState = jobAdmin.deleteJob(jobKey);
		return ResponseEntity.ok(JobResult.success(jobState, "ok"));
	}

	@PostMapping("/triggerJob")
	public ResponseEntity<JobResult> triggerJob(@RequestBody JobParam jobParam) {
		JobState jobState = jobAdmin.triggerJob(jobParam.getJobKey(), jobParam.getAttachment());
		return ResponseEntity.ok(JobResult.success(jobState, "ok"));
	}

}
