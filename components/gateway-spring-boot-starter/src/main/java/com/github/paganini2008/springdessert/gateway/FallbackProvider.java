package com.github.paganini2008.springdessert.gateway;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClientException;

/**
 * 
 * FallbackProvider
 * 
 * @author Fred Feng
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

	public String getBody(Router route, RestClientException e);

}
