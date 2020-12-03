package com.github.paganini2008.springdessert.cluster.http;

import java.util.Map;

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

	String getPath();

	HttpMethod getMethod();

	HttpHeaders getHeaders();

	Map<String, Object> getRequestParameters();

	Map<String, Object> getPathVariables();

	HttpEntity<Object> getBody();

	long getTimestamp();

}