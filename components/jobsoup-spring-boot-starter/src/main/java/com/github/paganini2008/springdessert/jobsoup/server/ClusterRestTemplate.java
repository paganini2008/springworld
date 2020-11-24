package com.github.paganini2008.springdessert.jobsoup.server;

import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.github.paganini2008.devtools.ArrayUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * ClusterRestTemplate
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Slf4j
public abstract class ClusterRestTemplate extends RestTemplate {

	public ClusterRestTemplate() {
		super();
	}

	public ClusterRestTemplate(ClientHttpRequestFactory httpRequestFactory) {
		super(httpRequestFactory);
	}

	public ClusterRestTemplate(List<HttpMessageConverter<?>> messageConverters) {
		super(messageConverters);
	}

	private final MultiValueMap<String, String> defaultHeaders = new LinkedMultiValueMap<String, String>();

	public void addHeader(String headerName, String headerValue) {
		defaultHeaders.add(headerName, headerValue);
	}

	public void setHeader(String headerName, String headerValue) {
		defaultHeaders.set(headerName, headerValue);
	}

	public <R> ResponseEntity<R> perform(String clusterName, String path, HttpMethod method, Object body,
			ParameterizedTypeReference<R> responseType) {
		String[] contextPaths = getClusterContextPaths(clusterName);
		if (ArrayUtils.isEmpty(contextPaths)) {
			throw new NoJobResourceException(clusterName);
		}
		String url;
		RestClientException reason = null;
		for (String contextPath : contextPaths) {
			url = contextPath + path;
			if (log.isTraceEnabled()) {
				log.trace("Perform job with url: " + url);
			}
			try {
				return super.exchange(url, method, new HttpEntity<Object>(body, getHttpHeaders()), responseType);
			} catch (RestClientException e) {
				log.error(e.getMessage(), e);
				reason = e;
			}
		}
		throw reason;
	}

	protected abstract String[] getClusterContextPaths(String clusterName);

	protected HttpHeaders getHttpHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.addAll(defaultHeaders);
		return headers;
	}

}
