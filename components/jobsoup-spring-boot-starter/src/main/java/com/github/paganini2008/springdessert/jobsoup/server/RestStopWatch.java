package com.github.paganini2008.springdessert.jobsoup.server;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;

import com.github.paganini2008.springdessert.jobsoup.JobKey;
import com.github.paganini2008.springdessert.jobsoup.JobState;
import com.github.paganini2008.springdessert.jobsoup.RunningState;
import com.github.paganini2008.springdessert.jobsoup.StopWatch;
import com.github.paganini2008.springdessert.jobsoup.model.JobResult;
import com.github.paganini2008.springdessert.jobsoup.model.JobRuntimeParam;

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
	public JobState onJobBegin(long traceId, JobKey jobKey, Date startDate) {
		ResponseEntity<JobResult<JobState>> responseEntity = restTemplate.perform(jobKey.getClusterName(), "/job/manager/onJobBegin",
				HttpMethod.POST, new JobRuntimeParam(traceId, jobKey, startDate), new ParameterizedTypeReference<JobResult<JobState>>() {
				});
		return responseEntity.getBody().getData();
	}

	@Async
	@Override
	public JobState onJobEnd(long traceId, JobKey jobKey, Date startDate, RunningState runningState, int retries) {
		ResponseEntity<JobResult<JobState>> responseEntity = restTemplate.perform(jobKey.getClusterName(), "/job/manager/onJobEnd",
				HttpMethod.POST, new JobRuntimeParam(traceId, jobKey, startDate, runningState, retries),
				new ParameterizedTypeReference<JobResult<JobState>>() {
				});
		return responseEntity.getBody().getData();

	}

}
