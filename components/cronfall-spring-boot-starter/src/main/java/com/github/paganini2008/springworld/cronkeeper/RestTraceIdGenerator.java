package com.github.paganini2008.springworld.cronkeeper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import com.github.paganini2008.springworld.cronkeeper.model.JobResult;
import com.github.paganini2008.springworld.cronkeeper.server.ClusterRestTemplate;

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
				HttpMethod.GET, null, new ParameterizedTypeReference<JobResult<Long>>() {
				});
		return responseEntity.getBody().getData();
	}

}
