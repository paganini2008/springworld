package com.github.paganini2008.springworld.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * JobBeanProxy
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Slf4j
public class JobBeanProxy implements Job {

	private final JobParameter jobParameter;

	@Qualifier("scheduler-httpclient")
	@Autowired
	private RestTemplate restTemplate;

	@Value("${spring.application.cluster.scheduler.server.hostUrl}")
	private String hostUrl;

	public JobBeanProxy(JobParameter jobParameter) {
		this.jobParameter = jobParameter;
	}

	@Override
	public String getSignature() {
		return jobParameter.getSignature();
	}

	@Override
	public String getJobName() {
		return jobParameter.getJobName();
	}

	@Override
	public String getJobClassName() {
		return jobParameter.getJobClassName();
	}

	@Override
	public String getGroupName() {
		return jobParameter.getGroupName();
	}

	@Override
	public boolean managedByApplicationContext() {
		return false;
	}

	@Override
	public Object execute(Object result) {
		final String url = hostUrl + "/job/run";
		HttpHeaders headers = new HttpHeaders();
		MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
		headers.setContentType(type);
		HttpEntity<JobParameter> requestEntity = new HttpEntity<JobParameter>(jobParameter, headers);
		ResponseEntity<JobResult> responseEntity = restTemplate.exchange(url, HttpMethod.POST, requestEntity, JobResult.class);
		log.info(responseEntity.toString());
		if (responseEntity.getStatusCode() == HttpStatus.OK) {
			JobResult jobResult = responseEntity.getBody();
			if (jobResult.getJobState() == JobState.FINISHED) {
				throw new JobTerminationException(this);
			}
		}
		return null;
	}

	@Override
	public boolean onFailure(Throwable e) {
		if (e instanceof JobTerminationException) {
			return false;
		}
		log.error(e.getMessage(), e);
		return true;
	}

}
