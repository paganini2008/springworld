package com.github.paganini2008.springworld.jobclick.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;

import com.github.paganini2008.springworld.jobclick.JobKey;
import com.github.paganini2008.springworld.jobclick.LogLevel;
import com.github.paganini2008.springworld.jobclick.LogManager;
import com.github.paganini2008.springworld.jobclick.model.JobLogParam;
import com.github.paganini2008.springworld.jobclick.model.JobResult;

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

	@Async
	@Override
	public void log(long traceId, JobKey jobKey, LogLevel logLevel, String messagePattern, Object[] args, String[] stackTraces) {
		ResponseEntity<JobResult<String>> responseEntity = restTemplate.perform(jobKey.getClusterName(), "/job/manager/log",
				HttpMethod.POST, new JobLogParam(traceId, jobKey, logLevel, messagePattern, args, stackTraces),
				new ParameterizedTypeReference<JobResult<String>>() {
				});
		responseEntity.getBody().getData();
	}

	@Override
	public void log(long traceId, LogLevel level, JobKey jobKey, String msg, String[] stackTraces) {
		throw new UnsupportedOperationException("log");
	}

	@Override
	public void error(long traceId, JobKey jobKey, String msg, String[] stackTraces) {
		throw new UnsupportedOperationException("error");
	}

}
