package com.github.paganini2008.springworld.crontab;

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

	@Autowired
	private ScheduleAdmin scheduleAdmin;

	@PostMapping("/persistJob")
	public ResponseEntity<JobResult<JobState>> persistJob(@RequestBody JobConfig jobConfig) throws Exception {
		JobState jobState = jobAdmin.persistJob(jobConfig);
		return ResponseEntity.ok(JobResult.success(jobState));
	}

	@PostMapping("/hasJob")
	public ResponseEntity<JobResult<JobState>> hasJob(@RequestBody JobKey jobKey) throws Exception {
		JobState jobState = jobAdmin.hasJob(jobKey);
		return ResponseEntity.ok(JobResult.success(jobState));
	}

	@DeleteMapping("/deleteJob")
	public ResponseEntity<JobResult<JobState>> deleteJob(@RequestBody JobKey jobKey) throws Exception {
		JobState jobState = jobAdmin.deleteJob(jobKey);
		return ResponseEntity.ok(JobResult.success(jobState));
	}

	@PostMapping("/triggerJob")
	public ResponseEntity<JobResult<JobState>> triggerJob(@RequestBody JobParam jobParam) throws Exception {
		JobState jobState = jobAdmin.triggerJob(jobParam.getJobKey(), jobParam.getAttachment());
		return ResponseEntity.ok(JobResult.success(jobState));
	}

	@PostMapping("/unscheduleJob")
	public ResponseEntity<JobResult<JobState>> unscheduleJob(@RequestBody JobKey jobKey) {
		JobState jobState = scheduleAdmin.unscheduleJob(jobKey);
		return ResponseEntity.ok(JobResult.success(jobState));
	}

	@PostMapping("/scheduleJob")
	public ResponseEntity<JobResult<JobState>> scheduleJob(@RequestBody JobKey jobKey) {
		JobState jobState = scheduleAdmin.scheduleJob(jobKey);
		return ResponseEntity.ok(JobResult.success(jobState));
	}

}
