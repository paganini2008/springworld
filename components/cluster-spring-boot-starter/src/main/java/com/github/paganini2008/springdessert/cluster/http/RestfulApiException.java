package com.github.paganini2008.springdessert.cluster.http;

import org.springframework.web.client.RestClientException;

/**
 * 
 * RestfulApiException
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class RestfulApiException extends RestClientException {

	private static final long serialVersionUID = -8762523199569525919L;

	public RestfulApiException(String msg) {
		super(msg);
	}

	public RestfulApiException(String msg, Throwable e) {
		super(msg, e);
	}

}
