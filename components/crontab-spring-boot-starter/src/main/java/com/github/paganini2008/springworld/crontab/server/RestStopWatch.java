package com.github.paganini2008.springworld.crontab.server;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import com.github.paganini2008.springworld.crontab.JobKey;
import com.github.paganini2008.springworld.crontab.JobState;
import com.github.paganini2008.springworld.crontab.RunningState;
import com.github.paganini2008.springworld.crontab.StopWatch;
import com.github.paganini2008.springworld.crontab.model.JobResult;
import com.github.paganini2008.springworld.crontab.model.JobRuntimeParam;

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

	@Override
	public JobState startJob(long traceId, JobKey jobKey, Date startTime) {
		ResponseEntity<JobResult<JobState>> responseEntity = restTemplate.perform(jobKey.getClusterName(), "/job/manager/startJob",
				HttpMethod.POST, new JobRuntimeParam(traceId, jobKey, startTime), new ParameterizedTypeReference<JobResult<JobState>>() {
				});
		return responseEntity.getBody().getData();
	}

	@Override
	public JobState finishJob(long traceId, JobKey jobKey, Date startTime, RunningState runningState, String[] errorStackTracks,
			int retries) {
		ResponseEntity<JobResult<JobState>> responseEntity = restTemplate.perform(jobKey.getClusterName(), "/job/manager/finishJob",
				HttpMethod.POST, new JobRuntimeParam(traceId, jobKey, startTime, runningState, errorStackTracks, retries),
				new ParameterizedTypeReference<JobResult<JobState>>() {
				});
		return responseEntity.getBody().getData();
	}

}
