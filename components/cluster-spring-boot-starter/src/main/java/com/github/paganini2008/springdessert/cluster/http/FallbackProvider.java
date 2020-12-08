package com.github.paganini2008.springdessert.cluster.http;

import java.lang.reflect.Method;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClientException;

/**
 * 
 * FallbackProvider
 *
 * @author Jimmy Hoff
 * 
 * @since 1.0
 */
public interface FallbackProvider {

	default HttpStatus getHttpStatus() {
		return HttpStatus.OK;
	}

	default HttpHeaders getHeaders() {
		return new HttpHeaders();
	}

	public Object getBody(String provider, Method method, Object[] arguments, RestClientException e);

}
