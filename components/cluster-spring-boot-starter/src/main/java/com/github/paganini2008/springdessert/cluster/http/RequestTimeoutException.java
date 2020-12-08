package com.github.paganini2008.springdessert.cluster.http;

/**
 * 
 * RequestTimeoutException
 *
 * @author Jimmy Hoff
 * 
 * @since 1.0
 */
public class RequestTimeoutException extends RestfulException {

	private static final long serialVersionUID = 5283485435591595177L;

	public RequestTimeoutException(Request request) {
		this(request.toString(), request);
	}

	public RequestTimeoutException(String msg, Request request) {
		super(msg, request);
	}

}
