package com.github.paganini2008.springdessert.webcrawler;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.github.paganini2008.devtools.RandomUtils;
import com.github.paganini2008.devtools.collection.MapUtils;

/**
 * 
 * HttpClientPageExtractor
 *
 * @author Fred Feng
 * @since 1.0
 */
public class HttpClientPageExtractor implements PageExtractor {

	private final RestTemplate restTemplate;

	public HttpClientPageExtractor() {
		this(new RestTemplate());
	}

	public HttpClientPageExtractor(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	public String extractHtml(String refer, String url) throws Exception {
		HttpHeaders headers = new HttpHeaders();
		headers.add("User-Agent", RandomUtils.randomChoice(userAgents));
		headers.add("X-Forwarded-For", RandomIpUtils.randomIp());
		MultiValueMap<String, String> defaultHeaders = getDefaultHeaders();
		if (MapUtils.isNotEmpty(defaultHeaders)) {
			headers.addAll(defaultHeaders);
		}
		ResponseEntity<String> responseEntity;
		responseEntity = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);
		if (responseEntity.getStatusCode() == HttpStatus.OK) {
			return responseEntity.getBody();
		}
		throw new PageExtractorException(url, responseEntity.getStatusCode());
	}

	protected MultiValueMap<String, String> getDefaultHeaders() {
		return null;
	}

}
