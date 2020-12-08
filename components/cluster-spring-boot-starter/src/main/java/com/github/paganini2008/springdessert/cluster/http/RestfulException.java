package com.github.paganini2008.springdessert.cluster.http;

import org.springframework.web.client.RestClientException;

/**
 * 
 * RestfulException
 *
 * @author Jimmy Hoff
 * 
 * @since 1.0
 */
public class RestfulException extends RestClientException {

	private static final long serialVersionUID = -8762523199569525919L;

	public RestfulException(String msg, Request request) {
		super(msg);
		this.request = request;
	}

	public RestfulException(String msg, Throwable e, Request request) {
		super(msg, e);
		this.request = request;
	}

	private final Request request;

	public Request getRequest() {
		return request;
	}

}
