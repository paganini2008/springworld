package com.github.paganini2008.springworld.cronkeeper.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import com.github.paganini2008.springworld.cronkeeper.JobAdmin;
import com.github.paganini2008.springworld.cronkeeper.JobDefinition;
import com.github.paganini2008.springworld.cronkeeper.JobKey;
import com.github.paganini2008.springworld.cronkeeper.JobManager;
import com.github.paganini2008.springworld.cronkeeper.JobPersistRequest;
import com.github.paganini2008.springworld.cronkeeper.JobState;
import com.github.paganini2008.springworld.cronkeeper.model.JobParam;
import com.github.paganini2008.springworld.cronkeeper.model.JobPersistParam;
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
	private JobManager jobManager;

	@Autowired
	private ClusterRestTemplate restTemplate;

	public JobState persistJob(JobPersistParam jobConfig) throws Exception {
		JobDefinition jobDef = JobPersistRequest.build(jobConfig);
		jobManager.persistJob(jobDef, jobConfig.getAttachment());
		return jobManager.getJobRuntime(JobKey.of(jobDef)).getJobState();
	}

	public JobState deleteJob(JobKey jobKey) throws Exception {
		return jobManager.deleteJob(jobKey);
	}

	public JobState hasJob(JobKey jobKey) throws Exception {
		if (jobManager.hasJob(jobKey)) {
			return jobManager.getJobRuntime(jobKey).getJobState();
		} else {
			return JobState.NONE;
		}
	}

	@Override
	public JobState triggerJob(JobKey jobKey, Object attachment) throws Exception {
		ResponseEntity<JobResult<JobState>> responseEntity = restTemplate.perform(jobKey.getClusterName(), "/job/admin/triggerJob",
				HttpMethod.POST, new JobParam(jobKey, attachment, 0), new ParameterizedTypeReference<JobResult<JobState>>() {
				});
		return responseEntity.getBody().getData();
	}

}
