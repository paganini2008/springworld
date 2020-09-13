package com.github.paganini2008.springworld.cronkeeper.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import com.github.paganini2008.springworld.cronkeeper.JobKey;
import com.github.paganini2008.springworld.cronkeeper.LogLevel;
import com.github.paganini2008.springworld.cronkeeper.LogManager;
import com.github.paganini2008.springworld.cronkeeper.model.JobLogParam;
import com.github.paganini2008.springworld.cronkeeper.model.JobResult;

/**
 * 
 * RestLogManager
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class RestLogManager implements LogManager {

	@Autowired
	private ClusterRestTemplate restTemplate;

	@Override
	public void log(long traceId, JobKey jobKey, LogLevel logLevel, String messagePattern, Object[] args, String[] stackTraces) {
		ResponseEntity<JobResult<String>> responseEntity = restTemplate.perform(jobKey.getClusterName(), "/job/manager/log",
				HttpMethod.POST, new JobLogParam(traceId, jobKey, logLevel, messagePattern, args, stackTraces),
				new ParameterizedTypeReference<JobResult<String>>() {
				});
		responseEntity.getBody().getData();
	}

	@Override
	public void error(long traceId, JobKey jobKey, String[] stackTraces) {
		ResponseEntity<JobResult<String>> responseEntity = restTemplate.perform(jobKey.getClusterName(), "/job/manager/error",
				HttpMethod.POST, new JobLogParam(traceId, jobKey, stackTraces), new ParameterizedTypeReference<JobResult<String>>() {
				});
		responseEntity.getBody().getData();
	}

}
