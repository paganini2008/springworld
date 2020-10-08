package com.github.paganini2008.springworld.joblink.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import com.github.paganini2008.springworld.joblink.JobKey;
import com.github.paganini2008.springworld.joblink.TraceIdGenerator;
import com.github.paganini2008.springworld.joblink.model.JobResult;

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
