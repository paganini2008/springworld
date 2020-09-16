package com.github.paganini2008.springworld.cronkeeper.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import com.github.paganini2008.springworld.cronkeeper.JobAdmin;
import com.github.paganini2008.springworld.cronkeeper.JobKey;
import com.github.paganini2008.springworld.cronkeeper.JobState;
import com.github.paganini2008.springworld.cronkeeper.model.JobParam;
import com.github.paganini2008.springworld.cronkeeper.model.JobResult;

/**
 * 
 * ServerModeJobAdmin
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class ServerModeJobAdmin implements JobAdmin {

	@Autowired
	private ClusterRestTemplate restTemplate;

	@Override
	public JobState triggerJob(JobKey jobKey, Object attachment) throws Exception {
		ResponseEntity<JobResult<JobState>> responseEntity = restTemplate.perform(jobKey.getClusterName(), "/job/admin/triggerJob",
				HttpMethod.POST, new JobParam(jobKey, attachment, 0), new ParameterizedTypeReference<JobResult<JobState>>() {
				});
		return responseEntity.getBody().getData();
	}

}
