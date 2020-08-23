package com.github.paganini2008.springworld.scheduler;

import java.util.List;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
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
public class ServerModeJobAdmin extends RestTemplate implements JobAdmin {

	public ServerModeJobAdmin() {
		super();
	}

	public ServerModeJobAdmin(ClientHttpRequestFactory requestFactory) {
		super(requestFactory);
	}

	public ServerModeJobAdmin(List<HttpMessageConverter<?>> messageConverters) {
		super(messageConverters);
	}

	@Value("${spring.application.cluster.scheduler.server.targetHostUrl:}")
	private String hostUrls;

	private final MultiValueMap<String, String> defaultHeaders = new LinkedMultiValueMap<String, String>();

	public MultiValueMap<String, String> getDefaultHeaders() {
		return defaultHeaders;
	}

	@Override
	public JobState persistJob(JobConfig jobConfig) {
		ResponseEntity<JobResult> responseEntity = doRequest(hostUrl -> {
			String url = hostUrl + "/job/admin/persistJob";
			HttpHeaders headers = new HttpHeaders();
			MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
			headers.setContentType(type);
			headers.addAll(defaultHeaders);
			HttpEntity<JobConfig> requestEntity = new HttpEntity<JobConfig>(jobConfig, headers);
			return this.exchange(url, HttpMethod.POST, requestEntity, JobResult.class);
		});
		if (responseEntity != null) {
			if (responseEntity.getStatusCode() == HttpStatus.OK) {
				if (log.isInfoEnabled()) {
					log.info(responseEntity.toString());
				}
				return responseEntity.getBody().getJobState();
			} else {
				throw new RestClientException("Bad state of restClient: " + responseEntity);
			}
		}
		throw new RestClientException("Unknown state of restClient: " + responseEntity);
	}

	@Override
	public JobState deleteJob(JobKey jobKey) {
		ResponseEntity<JobResult> responseEntity = doRequest(hostUrl -> {
			String url = hostUrl + "/job/admin/deleteJob";
			log.info("Delete job '{}' on {}", jobKey, url);
			HttpHeaders headers = new HttpHeaders();
			MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
			headers.setContentType(type);
			headers.addAll(defaultHeaders);
			HttpEntity<JobKey> requestEntity = new HttpEntity<JobKey>(jobKey, headers);
			return this.exchange(url, HttpMethod.DELETE, requestEntity, JobResult.class);
		});
		if (responseEntity != null) {
			if (responseEntity.getStatusCode() == HttpStatus.OK) {
				if (log.isInfoEnabled()) {
					log.info(responseEntity.toString());
				}
				return responseEntity.getBody().getJobState();
			} else {
				throw new RestClientException("Bad state of restClient: " + responseEntity);
			}
		}
		throw new RestClientException("Unknown state of restClient: " + responseEntity);
	}

	@Override
	@Retryable(value = RestClientException.class, maxAttempts = 3, backoff = @Backoff(delay = 5000L, multiplier = 1))
	public JobState hasJob(JobKey jobKey) {
		ResponseEntity<JobResult> responseEntity = doRequest(hostUrl -> {
			String url = hostUrl + "/job/admin/hasJob";
			log.info("Check job '{}' existed on {}", jobKey, url);
			HttpHeaders headers = new HttpHeaders();
			MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
			headers.setContentType(type);
			headers.addAll(defaultHeaders);
			HttpEntity<JobKey> requestEntity = new HttpEntity<JobKey>(jobKey, headers);
			return this.exchange(url, HttpMethod.POST, requestEntity, JobResult.class);
		});
		if (responseEntity != null) {
			if (responseEntity.getStatusCode() == HttpStatus.OK) {
				if (log.isInfoEnabled()) {
					log.info(responseEntity.toString());
				}
				return responseEntity.getBody().getJobState();
			} else {
				throw new RestClientException("Bad state of restClient: " + responseEntity);
			}
		}
		throw new RestClientException("Unknown state of restClient: " + responseEntity);
	}

	@Override
	@Retryable(value = RestClientException.class, maxAttempts = 3, backoff = @Backoff(delay = 5000L, multiplier = 1))
	public JobState triggerJob(JobKey jobKey, Object attachment) {
		ResponseEntity<JobResult> responseEntity = doRequest(hostUrl -> {
			String url = hostUrl + "/job/admin/triggerJob";
			log.info("Trigger job '{}' on {}", jobKey, url);
			HttpHeaders headers = new HttpHeaders();
			MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
			headers.setContentType(type);
			headers.addAll(defaultHeaders);
			HttpEntity<JobParam> requestEntity = new HttpEntity<JobParam>(new JobParam(jobKey, attachment), headers);
			return this.exchange(url, HttpMethod.POST, requestEntity, JobResult.class);
		});
		if (responseEntity != null) {
			if (responseEntity.getStatusCode() == HttpStatus.OK) {
				if (log.isInfoEnabled()) {
					log.info(responseEntity.toString());
				}
				JobResult jobResult = responseEntity.getBody();
				if (jobResult.getJobState() == JobState.FINISHED) {
					throw new JobTerminationException(jobKey);
				}
			} else {
				throw new RestClientException("Bad state of restClient: " + responseEntity);
			}
		}
		throw new RestClientException("Unknown state of restClient: " + responseEntity);
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
			} catch (JobTerminationException e) {
				throw e;
			} catch (RestClientException e) {
				reason = e;
			}
		}
		throw new RestClientException("Unknown state of restClient: " + reason.getMessage(), reason);
	}

}
