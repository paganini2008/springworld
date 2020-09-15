package com.github.paganini2008.springworld.cronkeeper.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.paganini2008.springworld.cronkeeper.JobKey;
import com.github.paganini2008.springworld.cronkeeper.JobManager;
import com.github.paganini2008.springworld.cronkeeper.JobState;
import com.github.paganini2008.springworld.cronkeeper.LogManager;
import com.github.paganini2008.springworld.cronkeeper.StopWatch;
import com.github.paganini2008.springworld.cronkeeper.TraceIdGenerator;
import com.github.paganini2008.springworld.cronkeeper.model.JobDetail;
import com.github.paganini2008.springworld.cronkeeper.model.JobLog;
import com.github.paganini2008.springworld.cronkeeper.model.JobLogParam;
import com.github.paganini2008.springworld.cronkeeper.model.JobQuery;
import com.github.paganini2008.springworld.cronkeeper.model.JobResult;
import com.github.paganini2008.springworld.cronkeeper.model.JobRuntime;
import com.github.paganini2008.springworld.cronkeeper.model.JobRuntimeParam;
import com.github.paganini2008.springworld.cronkeeper.model.JobStackTrace;
import com.github.paganini2008.springworld.cronkeeper.model.JobStateParam;
import com.github.paganini2008.springworld.cronkeeper.model.JobTrace;
import com.github.paganini2008.springworld.cronkeeper.model.JobTracePageQuery;
import com.github.paganini2008.springworld.cronkeeper.model.JobTraceQuery;
import com.github.paganini2008.springworld.cronkeeper.model.JobTriggerDetail;
import com.github.paganini2008.springworld.cronkeeper.model.PageQuery;

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
	private LogManager logManager;

	@Autowired
	private StopWatch stopWatch;

	@Autowired
	private TraceIdGenerator traceIdGenerator;

	@PostMapping("/getJobDetail")
	public ResponseEntity<JobResult<JobDetail>> getJobDetail(@RequestBody JobKey jobKey) throws Exception {
		JobDetail jobDetail = jobManager.getJobDetail(jobKey, true);
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

	@PostMapping("/selectJobDetail")
	public ResponseEntity<JobResult<PageQuery<JobDetail>>> selectJobDetail(@RequestBody PageQuery<JobDetail> pageQuery) throws Exception {
		jobManager.selectJobDetail(pageQuery);
		return ResponseEntity.ok(JobResult.success(pageQuery));
	}

	@PostMapping("/selectJobTrace")
	public ResponseEntity<JobResult<PageQuery<JobTrace>>> selectJobTrace(@RequestBody JobTracePageQuery<JobTrace> pageQuery)
			throws Exception {
		jobManager.selectJobTrace(pageQuery);
		return ResponseEntity.ok(JobResult.success(pageQuery));
	}

	@PostMapping("/selectJobLog")
	public ResponseEntity<JobResult<JobLog[]>> selectJobLog(@RequestBody JobTraceQuery query) throws Exception {
		JobLog[] logs = jobManager.selectJobLog(query);
		return ResponseEntity.ok(JobResult.success(logs));
	}

	@PostMapping("/selectJobStackTrace")
	public ResponseEntity<JobResult<JobStackTrace[]>> selectJobStackTrace(@RequestBody JobTraceQuery query) throws Exception {
		JobStackTrace[] traces = jobManager.selectJobStackTrace(query);
		return ResponseEntity.ok(JobResult.success(traces));
	}

	@PostMapping("/startJob")
	public ResponseEntity<JobResult<JobState>> startJob(@RequestBody JobRuntimeParam param) throws Exception {
		JobState jobState = stopWatch.startJob(param.getTraceId(), param.getJobKey(), param.getStartTime());
		return ResponseEntity.ok(JobResult.success(jobState));
	}

	@PostMapping("/finishJob")
	public ResponseEntity<JobResult<JobState>> finishJob(@RequestBody JobRuntimeParam param) throws Exception {
		JobState jobState = stopWatch.finishJob(param.getTraceId(), param.getJobKey(), param.getStartTime(), param.getRunningState(),
				param.getRetries());
		return ResponseEntity.ok(JobResult.success(jobState));
	}

	@PostMapping("/generateTraceId")
	public ResponseEntity<JobResult<Long>> generateTraceId(@RequestBody JobKey jobKey) {
		long traceId = traceIdGenerator.generateTraceId(jobKey);
		return ResponseEntity.ok(JobResult.success(traceId));
	}

	@PostMapping("/log")
	public ResponseEntity<JobResult<String>> log(@RequestBody JobLogParam param) throws Exception {
		logManager.log(param.getTraceId(), param.getJobKey(), param.getLogLevel(), param.getMessagePattern(), param.getArgs(),
				param.getStackTraces());
		return ResponseEntity.ok(JobResult.success("ok"));
	}

}
