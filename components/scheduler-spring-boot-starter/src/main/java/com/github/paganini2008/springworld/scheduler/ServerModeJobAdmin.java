package com.github.paganini2008.springworld.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * ServerModeJobAdmin
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Slf4j
public class ServerModeJobAdmin implements JobAdmin {

	@Autowired
	private RestTemplate restTemplate;

	@Value("${spring.application.cluster.scheduler.server.hostUrl}")
	private String hostUrl;

	@Override
	public void addJob(JobConfig jobConfig) {
		final String url = hostUrl + "/job/manager/addJob";
		HttpHeaders headers = new HttpHeaders();
		MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
		headers.setContentType(type);
		HttpEntity<JobConfig> requestEntity = new HttpEntity<JobConfig>(jobConfig, headers);
		ResponseEntity<JobResult> responseEntity = null;
		try {
			responseEntity = restTemplate.exchange(url, HttpMethod.POST, requestEntity, JobResult.class);
			log.info(responseEntity.toString());
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new JobException(e.getMessage(), e);
		}
	}

	@Override
	public void deleteJob(JobKey jobKey) {
		final String url = hostUrl + "/job/manager/deleteJob";
		HttpHeaders headers = new HttpHeaders();
		MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
		headers.setContentType(type);
		HttpEntity<JobKey> requestEntity = new HttpEntity<JobKey>(jobKey, headers);
		ResponseEntity<JobResult> responseEntity = null;
		try {
			responseEntity = restTemplate.exchange(url, HttpMethod.POST, requestEntity, JobResult.class);
			log.info(responseEntity.toString());
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new JobException(e.getMessage(), e);
		}
	}

	@Override
	public boolean hasJob(JobKey jobKey) {
		final String url = hostUrl + "/job/manager/hasJob";
		HttpHeaders headers = new HttpHeaders();
		MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
		headers.setContentType(type);
		HttpEntity<JobKey> requestEntity = new HttpEntity<JobKey>(jobKey, headers);
		ResponseEntity<JobResult> responseEntity = null;
		try {
			responseEntity = restTemplate.exchange(url, HttpMethod.POST, requestEntity, JobResult.class);
			log.info(responseEntity.toString());
			return true;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new JobException(e.getMessage(), e);
		}
	}

}
