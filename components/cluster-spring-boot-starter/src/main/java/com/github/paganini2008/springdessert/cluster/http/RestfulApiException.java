package com.github.paganini2008.springdessert.cluster.http;

import org.springframework.web.client.RestClientException;

/**
 * 
 * RestfulApiException
 * 
 * @author Jimmy Hoff
 *
 * @since 1.0
 */
public class RestfulApiException extends RestClientException {

	private static final long serialVersionUID = -8762523199569525919L;
	private static final String NEWLINE = System.getProperty("line.separator");

	public RestfulApiException(Request request, String msg) {
		super(request.toString() + NEWLINE + msg);
		this.request = request;
	}

	public RestfulApiException(Request request, String msg, Throwable e) {
		super(request.toString() + NEWLINE + msg, e);
		this.request = request;
	}

	private final Request request;

	public Request getRequest() {
		return request;
	}

}
