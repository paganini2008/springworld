package com.github.paganini2008.springdessert.gateway;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.lang.Nullable;

import com.github.paganini2008.springdessert.cluster.http.Request;

/**
 * 
 * RoutingRequest
 *
 * @author Jimmy Hoff
 * 
 * @since 1.0
 */
public class RoutingRequest implements Request {

	private final String path;
	private final HttpMethod method;
	private final HttpHeaders headers;
	private final long timestamp;
	private final HttpEntity<Object> body;

	public RoutingRequest(String path, HttpMethod method, HttpHeaders headers, @Nullable Object body) {
		this.path = path;
		this.method = method;
		this.headers = headers;
		this.body = new HttpEntity<Object>(body);
		this.timestamp = System.currentTimeMillis();
	}

	@Override
	public String getPath() {
		return path;
	}

	@Override
	public HttpMethod getMethod() {
		return method;
	}

	@Override
	public HttpHeaders getHeaders() {
		return headers;
	}

	@Override
	public HttpEntity<Object> getBody() {
		return body;
	}

	@Override
	public long getTimestamp() {
		return timestamp;
	}

}
