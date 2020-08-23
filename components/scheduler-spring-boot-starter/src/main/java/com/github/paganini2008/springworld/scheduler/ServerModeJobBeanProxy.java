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
 * ServerModeJobBeanProxy
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Slf4j
public class ServerModeJobBeanProxy implements Job {

	private final JobKey jobKey;
	private final JobTriggerDetail triggerDetail;

	@Qualifier("scheduler-httpclient")
	@Autowired
	private RestTemplate restTemplate;

	@Value("${spring.application.cluster.scheduler.server.hostUrl}")
	private String hostUrl;

	public ServerModeJobBeanProxy(JobKey jobKey, JobTriggerDetail triggerDetail) {
		this.jobKey = jobKey;
		this.triggerDetail = triggerDetail;
	}

	@Override
	public String getJobName() {
		return jobKey.getJobName();
	}

	@Override
	public String getJobClassName() {
		return jobKey.getJobClassName();
	}

	@Override
	public String getGroupName() {
		return jobKey.getGroupName();
	}

	@Override
	public TriggerBuilder buildTrigger() {
		return TriggerBuilder.newTrigger(triggerDetail.getTriggerType()).setStartDate(triggerDetail.getStartDate())
				.setEndDate(triggerDetail.getEndDate()).setTriggerDescription(triggerDetail.getTriggerDescription());
	}

	@Override
	public boolean managedByApplicationContext() {
		return false;
	}

	@Override
	public Object execute(JobKey jobKey, Object result) {
		final String url = hostUrl + "/job/run";
		HttpHeaders headers = new HttpHeaders();
		MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
		headers.setContentType(type);
		HttpEntity<JobParam> requestEntity = new HttpEntity<JobParam>(new JobParam(jobKey, result), headers);
		ResponseEntity<JobResult> responseEntity = null;
		try {
			responseEntity = restTemplate.exchange(url, HttpMethod.POST, requestEntity, JobResult.class);
			log.info(responseEntity.toString());
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		if (responseEntity != null && responseEntity.getStatusCode() == HttpStatus.OK) {
			JobResult jobResult = responseEntity.getBody();
			if (jobResult.getJobState() == JobState.FINISHED) {
				throw new JobTerminationException(jobKey);
			}
			return null;
		}
		throw new JobException();
	}

	@Override
	public void onFailure(JobKey jobKey, Throwable e) {
		log.error(e.getMessage(), e);
	}

}
