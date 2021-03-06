package com.github.paganini2008.springdessert.jobsoup.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import com.github.paganini2008.springdessert.jobsoup.JobAdmin;
import com.github.paganini2008.springdessert.jobsoup.JobKey;
import com.github.paganini2008.springdessert.jobsoup.JobLifeCycle;
import com.github.paganini2008.springdessert.jobsoup.JobState;
import com.github.paganini2008.springdessert.jobsoup.model.JobLifeCycleParam;
import com.github.paganini2008.springdessert.jobsoup.model.JobParam;
import com.github.paganini2008.springdessert.jobsoup.model.JobResult;

/**
 * 
 * ServerModeJobAdmin
 * 
 * @author Jimmy Hoff
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
