package com.github.paganini2008.springdessert.jobsoup.server;

import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import com.github.paganini2008.springdessert.jobsoup.DependencyType;
import com.github.paganini2008.springdessert.jobsoup.JobDefinition;
import com.github.paganini2008.springdessert.jobsoup.JobKey;
import com.github.paganini2008.springdessert.jobsoup.JobManager;
import com.github.paganini2008.springdessert.jobsoup.JobState;
import com.github.paganini2008.springdessert.jobsoup.model.JobDependencyParam;
import com.github.paganini2008.springdessert.jobsoup.model.JobDetail;
import com.github.paganini2008.springdessert.jobsoup.model.JobKeyQuery;
import com.github.paganini2008.springdessert.jobsoup.model.JobLog;
import com.github.paganini2008.springdessert.jobsoup.model.JobPersistParam;
import com.github.paganini2008.springdessert.jobsoup.model.JobResult;
import com.github.paganini2008.springdessert.jobsoup.model.JobRuntime;
import com.github.paganini2008.springdessert.jobsoup.model.JobStackTrace;
import com.github.paganini2008.springdessert.jobsoup.model.JobStateParam;
import com.github.paganini2008.springdessert.jobsoup.model.JobTrace;
import com.github.paganini2008.springdessert.jobsoup.model.JobTracePageQuery;
import com.github.paganini2008.springdessert.jobsoup.model.JobTraceQuery;
import com.github.paganini2008.springdessert.jobsoup.model.JobTriggerDetail;
import com.github.paganini2008.springdessert.jobsoup.model.PageQuery;
import com.github.paganini2008.springdessert.jobsoup.utils.GenericJobDefinition;

/**
 * 
 * RestJobManager
 * 
 * @author Jimmy Hoff
 *
 * @since 1.0
 */
public class RestJobManager implements JobManager {

	@Autowired
	private ClusterRestTemplate restTemplate;

	@Override
	public String[] selectClusterNames() throws Exception {
		ResponseEntity<JobResult<String[]>> responseEntity = restTemplate.perform(null, "/job/manager/selectClusterNames", HttpMethod.GET,
				null, new ParameterizedTypeReference<JobResult<String[]>>() {
				});
		return responseEntity.getBody().getData();
	}

	@Override
	public int persistJob(JobPersistParam param) throws Exception {
		ResponseEntity<JobResult<Integer>> responseEntity = restTemplate.perform(param.getJobKey().getClusterName(),
				"/job/manager/persistJob", HttpMethod.POST, param, new ParameterizedTypeReference<JobResult<Integer>>() {
				});
		return responseEntity.getBody().getData();
	}

	@Override
	public int persistJob(JobDefinition jobDefinition, String attachment) throws Exception {
		if (!(jobDefinition instanceof GenericJobDefinition)) {
			throw new UnsupportedOperationException("Please use GenericJobDefinition.Builder to build a new job.");
		}
		GenericJobDefinition jobDef = (GenericJobDefinition) jobDefinition;
		return persistJob(jobDef.toParameter());
	}

	@Override
	public JobState finishJob(JobKey jobKey) throws Exception {
		ResponseEntity<JobResult<JobState>> responseEntity = restTemplate.perform(jobKey.getClusterName(), "/job/manager/finishJob",
				HttpMethod.POST, jobKey, new ParameterizedTypeReference<JobResult<JobState>>() {
				});
		return responseEntity.getBody().getData();
	}

	@Override
	public boolean hasJob(JobKey jobKey) throws Exception {
		ResponseEntity<JobResult<Boolean>> responseEntity = restTemplate.perform(jobKey.getClusterName(), "/job/manager/hasJob",
				HttpMethod.POST, jobKey, new ParameterizedTypeReference<JobResult<Boolean>>() {
				});
		return responseEntity.getBody().getData();
	}

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
		ResponseEntity<JobResult<Boolean>> responseEntity = restTemplate.perform(jobKey.getClusterName(), "/job/manager/hasJobState",
				HttpMethod.POST, new JobStateParam(jobKey, jobState), new ParameterizedTypeReference<JobResult<Boolean>>() {
				});
		return responseEntity.getBody().getData();
	}

	@Override
	public JobState setJobState(JobKey jobKey, JobState jobState) throws Exception {
		ResponseEntity<JobResult<JobState>> responseEntity = restTemplate.perform(jobKey.getClusterName(), "/job/manager/setJobState",
				HttpMethod.POST, new JobStateParam(jobKey, jobState), new ParameterizedTypeReference<JobResult<JobState>>() {
				});
		return responseEntity.getBody().getData();
	}

	@Override
	public JobDetail getJobDetail(JobKey jobKey, boolean detailed) throws Exception {
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
	public boolean hasRelations(JobKey jobKey, DependencyType dependencyType) throws Exception {
		ResponseEntity<JobResult<Boolean>> responseEntity = restTemplate.perform(jobKey.getClusterName(), "/job/manager/hasRelations",
				HttpMethod.POST, new JobDependencyParam(jobKey, dependencyType), new ParameterizedTypeReference<JobResult<Boolean>>() {
				});
		return responseEntity.getBody().getData();
	}

	@Override
	public JobKey[] getRelations(JobKey jobKey, DependencyType dependencyType) throws Exception {
		ResponseEntity<JobResult<JobKey[]>> responseEntity = restTemplate.perform(jobKey.getClusterName(), "/job/manager/getRelations",
				HttpMethod.POST, new JobDependencyParam(jobKey, dependencyType), new ParameterizedTypeReference<JobResult<JobKey[]>>() {
				});
		return responseEntity.getBody().getData();
	}

	@Override
	public JobKey[] getDependentKeys(JobKey jobKey, DependencyType dependencyType) throws Exception {
		ResponseEntity<JobResult<JobKey[]>> responseEntity = restTemplate.perform(jobKey.getClusterName(), "/job/manager/getDependentKeys",
				HttpMethod.POST, new JobDependencyParam(jobKey, dependencyType), new ParameterizedTypeReference<JobResult<JobKey[]>>() {
				});
		return responseEntity.getBody().getData();
	}

	@Override
	public JobKey[] getJobKeys(JobKeyQuery jobQuery) throws Exception {
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
		ResponseEntity<JobResult<Integer>> responseEntity = restTemplate.perform(jobKey.getClusterName(), "/job/manager/getJobId",
				HttpMethod.POST, jobKey, new ParameterizedTypeReference<JobResult<Integer>>() {
				});
		return responseEntity.getBody().getData();
	}

	@Override
	public void selectJobDetail(PageQuery<JobDetail> pageQuery) throws Exception {
		ResponseEntity<JobResult<PageQuery<JobDetail>>> responseEntity = restTemplate.perform(pageQuery.getClusterName(),
				"/job/manager/selectJobDetail", HttpMethod.POST, pageQuery,
				new ParameterizedTypeReference<JobResult<PageQuery<JobDetail>>>() {
				});
		PageQuery<JobDetail> data = responseEntity.getBody().getData();
		if (data != null) {
			pageQuery.setRows(data.getRows());
			pageQuery.setContent(data.getContent());
			pageQuery.setNextPage(data.isNextPage());
		}
	}

	@Override
	public void selectJobTrace(JobTracePageQuery<JobTrace> pageQuery) throws Exception {
		ResponseEntity<JobResult<PageQuery<JobTrace>>> responseEntity = restTemplate.perform(pageQuery.getClusterName(),
				"/job/manager/selectJobTrace", HttpMethod.POST, pageQuery,
				new ParameterizedTypeReference<JobResult<PageQuery<JobTrace>>>() {
				});
		PageQuery<JobTrace> data = responseEntity.getBody().getData();
		if (data != null) {
			pageQuery.setRows(data.getRows());
			pageQuery.setContent(data.getContent());
			pageQuery.setNextPage(data.isNextPage());
		}
	}

	@Override
	public JobStackTrace[] selectJobStackTrace(JobTraceQuery query) throws SQLException {
		ResponseEntity<JobResult<JobStackTrace[]>> responseEntity = restTemplate.perform(query.getClusterName(),
				"/job/manager/selectJobStackTrace", HttpMethod.POST, query, new ParameterizedTypeReference<JobResult<JobStackTrace[]>>() {
				});
		return responseEntity.getBody().getData();
	}

	@Override
	public JobLog[] selectJobLog(JobTraceQuery query) throws SQLException {
		ResponseEntity<JobResult<JobLog[]>> responseEntity = restTemplate.perform(query.getClusterName(), "/job/manager/selectJobLog",
				HttpMethod.POST, query, new ParameterizedTypeReference<JobResult<JobLog[]>>() {
				});
		return responseEntity.getBody().getData();
	}

}
