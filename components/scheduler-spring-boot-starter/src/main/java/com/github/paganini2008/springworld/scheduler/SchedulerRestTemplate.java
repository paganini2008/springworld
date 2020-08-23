package com.github.paganini2008.springworld.scheduler;

import java.util.List;

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
 * SchedulerRestTemplate
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Slf4j
public class SchedulerRestTemplate extends RestTemplate {

	public SchedulerRestTemplate() {
		super();
	}

	public SchedulerRestTemplate(ClientHttpRequestFactory requestFactory) {
		super(requestFactory);
	}

	public SchedulerRestTemplate(List<HttpMessageConverter<?>> messageConverters) {
		super(messageConverters);
	}

	@Value("${spring.application.cluster.scheduler.server.targetHostUrl:}")
	private String hostUrls;

	private final MultiValueMap<String, String> defaultHeaders = new LinkedMultiValueMap<String, String>();

	public MultiValueMap<String, String> getDefaultHeaders() {
		return defaultHeaders;
	}

	@Retryable(value = RestClientException.class, maxAttempts = 3, backoff = @Backoff(delay = 5000L, multiplier = 1))
	public void triggerJob(JobKey jobKey, Object result) {
		if (StringUtils.isBlank(hostUrls)) {
			log.warn("Configuration 'spring.application.cluster.scheduler.server.targetHostUrl' must be required.");
			return;
		}
		for (String hostUrl : hostUrls.split(",")) {
			String url = hostUrl + "/job/run";
			HttpHeaders headers = new HttpHeaders();
			MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
			headers.setContentType(type);
			headers.addAll(defaultHeaders);
			HttpEntity<JobParam> requestEntity = new HttpEntity<JobParam>(new JobParam(jobKey, result), headers);
			ResponseEntity<JobResult> responseEntity = null;
			responseEntity = this.exchange(url, HttpMethod.POST, requestEntity, JobResult.class);
			log.info("Trigger job '{}' on {}", jobKey, hostUrl);
			if (responseEntity != null) {
				if (responseEntity.getStatusCode() == HttpStatus.OK) {
					if (log.isInfoEnabled()) {
						log.info(responseEntity.toString());
					}
					JobResult jobResult = responseEntity.getBody();
					if (jobResult.getJobState() == JobState.FINISHED) {
						throw new JobTerminationException(jobKey);
					}
					break;
				} else {
					throw new RestClientException("Bad state of restClient: " + responseEntity);
				}
			}
			throw new RestClientException("Unknown state of restClient: " + responseEntity);
		}
	}

}
