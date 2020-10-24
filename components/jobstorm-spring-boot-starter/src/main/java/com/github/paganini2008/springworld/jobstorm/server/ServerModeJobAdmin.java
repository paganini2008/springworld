package com.github.paganini2008.springworld.jobstorm.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import com.github.paganini2008.springworld.jobstorm.JobAdmin;
import com.github.paganini2008.springworld.jobstorm.JobKey;
import com.github.paganini2008.springworld.jobstorm.JobLifeCycle;
import com.github.paganini2008.springworld.jobstorm.JobState;
import com.github.paganini2008.springworld.jobstorm.model.JobLifeCycleParam;
import com.github.paganini2008.springworld.jobstorm.model.JobParam;
import com.github.paganini2008.springworld.jobstorm.model.JobResult;

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

	@Override
	public void publicLifeCycleEvent(JobKey jobKey, JobLifeCycle lifeCycle) {
		ResponseEntity<JobResult<String>> responseEntity = restTemplate.perform(jobKey.getClusterName(), "/job/admin/publicLifeCycleEvent",
				HttpMethod.POST, new JobLifeCycleParam(jobKey, lifeCycle), new ParameterizedTypeReference<JobResult<String>>() {
				});
		responseEntity.getBody();
	}

}
