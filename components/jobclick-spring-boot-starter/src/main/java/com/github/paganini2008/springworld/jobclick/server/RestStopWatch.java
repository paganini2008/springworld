package com.github.paganini2008.springworld.jobclick.server;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;

import com.github.paganini2008.springworld.jobclick.JobKey;
import com.github.paganini2008.springworld.jobclick.JobState;
import com.github.paganini2008.springworld.jobclick.RunningState;
import com.github.paganini2008.springworld.jobclick.StopWatch;
import com.github.paganini2008.springworld.jobclick.model.JobResult;
import com.github.paganini2008.springworld.jobclick.model.JobRuntimeParam;

/**
 * 
 * RestStopWatch
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class RestStopWatch implements StopWatch {

	@Autowired
	private ClusterRestTemplate restTemplate;

	@Async
	@Override
	public JobState startJob(long traceId, JobKey jobKey, Date startTime) {
		ResponseEntity<JobResult<JobState>> responseEntity = restTemplate.perform(jobKey.getClusterName(), "/job/manager/startJob",
				HttpMethod.POST, new JobRuntimeParam(traceId, jobKey, startTime), new ParameterizedTypeReference<JobResult<JobState>>() {
				});
		return responseEntity.getBody().getData();
	}

	@Async
	@Override
	public JobState finishJob(long traceId, JobKey jobKey, Date startTime, RunningState runningState, int retries) {
		ResponseEntity<JobResult<JobState>> responseEntity = restTemplate.perform(jobKey.getClusterName(), "/job/manager/finishJob",
				HttpMethod.POST, new JobRuntimeParam(traceId, jobKey, startTime, runningState, retries),
				new ParameterizedTypeReference<JobResult<JobState>>() {
				});
		return responseEntity.getBody().getData();
	}

}
