package com.github.paganini2008.springworld.cronfall.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.paganini2008.springworld.cronfall.JobKey;
import com.github.paganini2008.springworld.cronfall.JobManager;
import com.github.paganini2008.springworld.cronfall.JobState;
import com.github.paganini2008.springworld.cronfall.StopWatch;
import com.github.paganini2008.springworld.cronfall.model.JobDetail;
import com.github.paganini2008.springworld.cronfall.model.JobQuery;
import com.github.paganini2008.springworld.cronfall.model.JobResult;
import com.github.paganini2008.springworld.cronfall.model.JobRuntime;
import com.github.paganini2008.springworld.cronfall.model.JobRuntimeParam;
import com.github.paganini2008.springworld.cronfall.model.JobStateParam;
import com.github.paganini2008.springworld.cronfall.model.JobTriggerDetail;

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
	public ResponseEntity<JobResult<JobState>> setJobState(@RequestBody JobStateParam param) throws Exception {
		JobState jobState = jobManager.setJobState(param.getJobKey(), param.getJobState());
		return ResponseEntity.ok(JobResult.success(jobState));
	}

	@PostMapping("/getJobKeys")
	public ResponseEntity<JobResult<JobKey[]>> getJobKeys(@RequestBody JobQuery jobQuery) throws Exception {
		JobKey[] jobKeys = jobManager.getJobKeys(jobQuery);
		return ResponseEntity.ok(JobResult.success(jobKeys));
	}

	@PostMapping("/startJob")
	public ResponseEntity<JobResult<JobState>> startJob(@RequestBody JobRuntimeParam param) throws Exception {
		JobState jobState = stopWatch.startJob(param.getTraceId(), param.getJobKey(), param.getStartTime());
		return ResponseEntity.ok(JobResult.success(jobState));
	}

	@PostMapping("/finishJob")
	public ResponseEntity<JobResult<JobState>> finishJob(@RequestBody JobRuntimeParam param) throws Exception {
		JobState jobState = stopWatch.finishJob(param.getTraceId(), param.getJobKey(), param.getStartTime(), param.getRunningState(),
				param.getStackTraces(), param.getRetries());
		return ResponseEntity.ok(JobResult.success(jobState));
	}

}
