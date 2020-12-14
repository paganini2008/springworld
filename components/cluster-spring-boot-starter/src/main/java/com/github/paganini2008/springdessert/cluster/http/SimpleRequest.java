package com.github.paganini2008.springdessert.cluster.http;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.lang.Nullable;

/**
 * 
 * SimpleRequest
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
public class SimpleRequest extends AbstractRequest implements Request {

	public SimpleRequest(String path, HttpMethod method, HttpHeaders headers) {
		this(path, method, headers, null);
	}

	public SimpleRequest(String path, HttpMethod method, HttpHeaders headers, @Nullable Object body) {
		super(path, method, headers);
		this.body = new HttpEntity<Object>(body, headers);
	}
	
	private HttpEntity<Object> body;

	public HttpEntity<Object> getBody() {
		return body;
	}

	public void setBody(HttpEntity<Object> body) {
		this.body = body;
	}

}
