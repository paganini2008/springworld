package com.github.paganini2008.springdessert.cluster.http;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

/**
 * 
 * Request
 * 
 * @author Jimmy Hoff
 *
 * @since 1.0
 */
public interface Request {

	static final String MAX_RETRY_COUNT = "REST_CLIENT_MAX_RETRY_COUNT";
	static final String MAX_TIMEOUT = "REST_CLIENT_MAX_TIMEOUT";
	static final String MAX_ALLOWED_PERMITS = "REST_CLIENT_MAX_ALLOWED_PERMITS";
	static final String FALLBACK = "REST_CLIENT_FALLBACK";

	String getPath();

	HttpMethod getMethod();

	HttpHeaders getHeaders();

	HttpEntity<Object> getBody();

	long getTimestamp();

}