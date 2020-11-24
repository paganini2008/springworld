package com.github.paganini2008.springdessert.jobsoup.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import com.github.paganini2008.springdessert.jobsoup.JobKey;
import com.github.paganini2008.springdessert.jobsoup.TraceIdGenerator;
import com.github.paganini2008.springdessert.jobsoup.model.JobResult;

/**
 * 
 * RestTraceIdGenerator
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class RestTraceIdGenerator implements TraceIdGenerator {

	@Autowired
	private ClusterRestTemplate restTemplate;

	@Override
	public long generateTraceId(JobKey jobKey) {
		ResponseEntity<JobResult<Long>> responseEntity = restTemplate.perform(jobKey.getClusterName(), "/job/manager/generateTraceId",
				HttpMethod.POST, jobKey, new ParameterizedTypeReference<JobResult<Long>>() {
				});
		return responseEntity.getBody().getData();
	}

}
