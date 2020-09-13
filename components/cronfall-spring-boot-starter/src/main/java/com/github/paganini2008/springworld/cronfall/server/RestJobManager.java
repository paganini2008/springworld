package com.github.paganini2008.springworld.cronfall.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import com.github.paganini2008.devtools.jdbc.ResultSetSlice;
import com.github.paganini2008.springworld.cronfall.JobKey;
import com.github.paganini2008.springworld.cronfall.JobManager;
import com.github.paganini2008.springworld.cronfall.JobState;
import com.github.paganini2008.springworld.cronfall.StatType;
import com.github.paganini2008.springworld.cronfall.model.JobDetail;
import com.github.paganini2008.springworld.cronfall.model.JobInfo;
import com.github.paganini2008.springworld.cronfall.model.JobQuery;
import com.github.paganini2008.springworld.cronfall.model.JobResult;
import com.github.paganini2008.springworld.cronfall.model.JobRuntime;
import com.github.paganini2008.springworld.cronfall.model.JobStat;
import com.github.paganini2008.springworld.cronfall.model.JobStateParam;
import com.github.paganini2008.springworld.cronfall.model.JobTriggerDetail;

/**
 * 
 * RestJobManager
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class RestJobManager implements JobManager {

	@Autowired
	private ClusterRestTemplate restTemplate;

	@Override
	public JobState pauseJob(JobKey jobKey) throws Exception {
		throw new UnsupportedOperationException("pauseJob");
	}

	@Override
	public JobState resumeJob(JobKey jobKey) throws Exception {
		throw new UnsupportedOperationException("resumeJob");
	}

	@Override
	public boolean hasJobState(JobKey jobKey, JobState jobState) throws Exception {
		throw new UnsupportedOperationException("hasJobState");
	}

	@Override
	public JobState setJobState(JobKey jobKey, JobState jobState) throws Exception {
		ResponseEntity<JobResult<JobState>> responseEntity = restTemplate.perform(jobKey.getClusterName(), "/job/manager/setJobState",
				HttpMethod.POST, new JobStateParam(jobKey, jobState), new ParameterizedTypeReference<JobResult<JobState>>() {
				});
		return responseEntity.getBody().getData();
	}

	@Override
	public JobDetail getJobDetail(JobKey jobKey) throws Exception {
		ResponseEntity<JobResult<JobDetail>> responseEntity = restTemplate.perform(jobKey.getClusterName(), "/job/manager/getJobDetail",
				HttpMethod.POST, jobKey, new ParameterizedTypeReference<JobResult<JobDetail>>() {
				});
		return responseEntity.getBody().getData();
	}

	@Override
	public JobTriggerDetail getJobTriggerDetail(JobKey jobKey) throws Exception {
		ResponseEntity<JobResult<JobTriggerDetail>> responseEntity = restTemplate.perform(jobKey.getClusterName(),
				"/job/manager/getJobTriggerDetail", HttpMethod.POST, jobKey, new ParameterizedTypeReference<JobResult<JobTriggerDetail>>() {
				});
		return responseEntity.getBody().getData();
	}

	@Override
	public boolean hasRelations(JobKey jobKey) throws Exception {
		ResponseEntity<JobResult<Boolean>> responseEntity = restTemplate.perform(jobKey.getClusterName(), "/job/manager/hasRelations",
				HttpMethod.POST, jobKey, new ParameterizedTypeReference<JobResult<Boolean>>() {
				});
		return responseEntity.getBody().getData();
	}

	@Override
	public JobKey[] getDependencies(JobKey jobKey) throws Exception {
		ResponseEntity<JobResult<JobKey[]>> responseEntity = restTemplate.perform(jobKey.getClusterName(), "/job/manager/getDependencies",
				HttpMethod.POST, jobKey, new ParameterizedTypeReference<JobResult<JobKey[]>>() {
				});
		return responseEntity.getBody().getData();
	}

	@Override
	public JobKey[] getJobKeys(JobQuery jobQuery) throws Exception {
		ResponseEntity<JobResult<JobKey[]>> responseEntity = restTemplate.perform(jobQuery.getClusterName(), "/job/manager/getJobKeys",
				HttpMethod.POST, jobQuery, new ParameterizedTypeReference<JobResult<JobKey[]>>() {
				});
		return responseEntity.getBody().getData();
	}

	@Override
	public JobRuntime getJobRuntime(JobKey jobKey) throws Exception {
		ResponseEntity<JobResult<JobRuntime>> responseEntity = restTemplate.perform(jobKey.getClusterName(), "/job/manager/getJobRuntime",
				HttpMethod.POST, jobKey, new ParameterizedTypeReference<JobResult<JobRuntime>>() {
				});
		return responseEntity.getBody().getData();
	}

	@Override
	public int getJobId(JobKey jobKey) throws Exception {
		throw new UnsupportedOperationException("getJobId");
	}

	@Override
	public JobStat getJobStat(JobKey jobKey) throws Exception {
		throw new UnsupportedOperationException("getJobStat");
	}

	@Override
	public ResultSetSlice<JobInfo> getJobInfo() throws Exception {
		throw new UnsupportedOperationException("getJobInfo");
	}

	@Override
	public ResultSetSlice<JobStat> getJobStat(StatType statType) throws Exception {
		throw new UnsupportedOperationException("getJobStat");
	}

}
