package com.github.paganini2008.springworld.crontab.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.paganini2008.springworld.crontab.JobKey;
import com.github.paganini2008.springworld.crontab.JobManager;
import com.github.paganini2008.springworld.crontab.JobParam;
import com.github.paganini2008.springworld.crontab.JobResult;
import com.github.paganini2008.springworld.crontab.JobRunningParam;
import com.github.paganini2008.springworld.crontab.JobState;
import com.github.paganini2008.springworld.crontab.StopWatch;
import com.github.paganini2008.springworld.crontab.model.JobDetail;
import com.github.paganini2008.springworld.crontab.model.JobQuery;
import com.github.paganini2008.springworld.crontab.model.JobRuntime;
import com.github.paganini2008.springworld.crontab.model.JobTriggerDetail;

/**
 * 
 * JobManagerController
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@RestController
@RequestMapping("/job/manager")
public class JobManagerController {

	@Autowired
	private JobManager jobManager;

	@Autowired
	private StopWatch stopWatch;

	@PostMapping("/getJobDetail")
	public ResponseEntity<JobResult<JobDetail>> getJobDetail(@RequestBody JobKey jobKey) throws Exception {
		JobDetail jobDetail = jobManager.getJobDetail(jobKey);
		return ResponseEntity.ok(JobResult.success(jobDetail));
	}

	@PostMapping("/getJobTriggerDetail")
	public ResponseEntity<JobResult<JobTriggerDetail>> getJobTriggerDetail(@RequestBody JobKey jobKey) throws Exception {
		JobTriggerDetail jobTriggerDetail = jobManager.getJobTriggerDetail(jobKey);
		return ResponseEntity.ok(JobResult.success(jobTriggerDetail));
	}

	@PostMapping("/getJobRuntime")
	public ResponseEntity<JobResult<JobRuntime>> getJobRuntime(@RequestBody JobKey jobKey) throws Exception {
		JobRuntime jobRuntime = jobManager.getJobRuntime(jobKey);
		return ResponseEntity.ok(JobResult.success(jobRuntime));
	}

	@PostMapping("/hasRelations")
	public ResponseEntity<JobResult<Boolean>> hasRelations(@RequestBody JobKey jobKey) throws Exception {
		boolean result = jobManager.hasRelations(jobKey);
		return ResponseEntity.ok(JobResult.success(result));
	}

	@PostMapping("/getDependencies")
	public ResponseEntity<JobResult<JobKey[]>> getDependencies(@RequestBody JobKey jobKey) throws Exception {
		JobKey[] jobKeys = jobManager.getDependencies(jobKey);
		return ResponseEntity.ok(JobResult.success(jobKeys));
	}

	@PostMapping("/setJobState")
	public ResponseEntity<JobResult<JobState>> setJobState(@RequestBody JobParam jobParam) throws Exception {
		JobState jobState = jobManager.setJobState(jobParam.getJobKey(), (JobState) jobParam.getAttachment());
		return ResponseEntity.ok(JobResult.success(jobState));
	}

	@PostMapping("/getJobKeys")
	public ResponseEntity<JobResult<JobKey[]>> getJobKeys(@RequestBody JobQuery jobQuery) throws Exception {
		JobKey[] jobKeys = jobManager.getJobKeys(jobQuery);
		return ResponseEntity.ok(JobResult.success(jobKeys));
	}

	@PostMapping("/startJob")
	public ResponseEntity<JobResult<JobState>> startJob(@RequestBody JobRunningParam param) throws Exception {
		JobState jobState = stopWatch.startJob(param.getJobKey(), param.getStartTime());
		return ResponseEntity.ok(JobResult.success(jobState));
	}

	@PostMapping("/finishJob")
	public ResponseEntity<JobResult<JobState>> finishJob(@RequestBody JobRunningParam param) throws Exception {
		JobState jobState = stopWatch.finishJob(param.getJobKey(), param.getStartTime(), param.getRunningState(),
				param.getErrorStackTracks());
		return ResponseEntity.ok(JobResult.success(jobState));
	}

}
