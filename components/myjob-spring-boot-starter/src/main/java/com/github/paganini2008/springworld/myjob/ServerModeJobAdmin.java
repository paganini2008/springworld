package com.github.paganini2008.springworld.myjob;

import java.sql.SQLException;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.github.paganini2008.devtools.StringUtils;

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
	private JobManager jobManager;

	@Autowired
	private RestTemplate restTemplate;

	@Value("${spring.application.cluster.scheduler.server.targetHostUrl:}")
	private String hostUrls;

	private final MultiValueMap<String, String> defaultHeaders = new LinkedMultiValueMap<String, String>();

	public MultiValueMap<String, String> getDefaultHeaders() {
		return defaultHeaders;
	}

	public JobState persistJob(JobConfig jobConfig) {
		JobDef jobDef = JobPersistRequest.build(jobConfig);
		try {
			jobManager.persistJob(jobDef, jobConfig.getAttachment());
			return jobManager.getJobRuntime(JobKey.of(jobDef)).getJobState();
		} catch (SQLException e) {
			throw new JobException(e.getMessage(), e);
		}

	}

	public JobState deleteJob(JobKey jobKey) {
		try {
			jobManager.deleteJob(jobKey);
			return jobManager.getJobRuntime(jobKey).getJobState();
		} catch (SQLException e) {
			throw new JobException(e.getMessage(), e);
		}
	}

	public JobState hasJob(JobKey jobKey) {
		try {
			if (jobManager.hasJob(jobKey)) {
				return jobManager.getJobRuntime(jobKey).getJobState();
			} else {
				return JobState.NONE;
			}
		} catch (SQLException e) {
			throw new JobException(e.getMessage(), e);
		}
	}

	@Override
	@Retryable(value = RestClientException.class, maxAttempts = 60, backoff = @Backoff(delay = 5000L, multiplier = 1))
	public JobState triggerJob(JobKey jobKey, Object attachment) {
		ResponseEntity<JobResult> responseEntity = null;
		try {
			responseEntity = doRequest(hostUrl -> {
				String url = hostUrl + "/job/admin/triggerJob";
				log.info("Trigger job '{}' on {}", jobKey, url);
				HttpHeaders headers = new HttpHeaders();
				MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
				headers.setContentType(type);
				headers.addAll(defaultHeaders);
				HttpEntity<JobParam> requestEntity = new HttpEntity<JobParam>(new JobParam(jobKey, attachment), headers);
				return restTemplate.exchange(url, HttpMethod.POST, requestEntity, JobResult.class);
			});
		} catch (RestClientException e) {
			throw e;
		} catch (JobException e) {
			throw e;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		if (responseEntity != null) {
			if (responseEntity.getStatusCode() == HttpStatus.OK) {
				if (log.isInfoEnabled()) {
					log.info(responseEntity.toString());
				}
				JobResult jobResult = responseEntity.getBody();
				if (jobResult.getJobState() == JobState.FINISHED) {
					throw new JobTerminationException(jobKey);
				}
				return responseEntity.getBody().getJobState();
			} else {
				throw new RestClientException("Bad state of restClient: " + responseEntity);
			}
		}
		return JobState.NONE;
	}

	private ResponseEntity<JobResult> doRequest(Function<String, ResponseEntity<JobResult>> f) {
		if (StringUtils.isBlank(hostUrls)) {
			log.warn("Configuration 'spring.application.cluster.scheduler.server.targetHostUrl' must be required.");
			return null;
		}
		Throwable reason = null;
		for (String hostUrl : hostUrls.split(",")) {
			try {
				return f.apply(hostUrl);
			} catch (JobException e) {
				throw e;
			} catch (Exception e) {
				reason = e;
			}
		}
		throw new RestClientException("Unknown state of restClient: " + reason.getMessage(), reason);
	}

}
