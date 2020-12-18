package com.github.paganini2008.springdessert.cluster.http;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

/**
 * 
 * AbstractRequest
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
public abstract class AbstractRequest implements Request {

	private final Map<String, Object> attributeMap = new HashMap<String, Object>();
	private final String path;
	private final HttpMethod method;
	private final HttpHeaders headers;
	private final long timestamp;

	protected AbstractRequest(String path, HttpMethod method, HttpHeaders headers) {
		super();
		this.path = path;
		this.method = method;
		this.headers = headers;
		this.timestamp = System.currentTimeMillis();
	}

	public void setAttribute(String attributeName, Object attributeValue) {
		if (attributeValue != null) {
			attributeMap.put(attributeName, attributeValue);
		} else {
			attributeMap.remove(attributeName);
		}
	}

	public Object getAttribute(String attributeName) {
		return getAttribute(attributeName, null);
	}

	public Object getAttribute(String attributeName, Object defaultValue) {
		return attributeMap.getOrDefault(attributeName, defaultValue);
	}

	public Map<String, Object> copyAttributes() {
		return Collections.unmodifiableMap(attributeMap);
	}

	public void clearAttributes() {
		attributeMap.clear();
	}

	public String getPath() {
		return path;
	}

	public HttpMethod getMethod() {
		return method;
	}

	public HttpHeaders getHeaders() {
		return headers;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public String toString() {
		return method.name().toUpperCase() + " " + path;
	}

}
