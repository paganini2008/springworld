package com.github.paganini2008.springdessert.cluster.http;

/**
 * 
 * TooManyRequestsException
 *
 * @author Jimmy Hoff
 * 
 * @since 1.0
 */
public class TooManyRequestsException extends RestfulException {

	private static final long serialVersionUID = 4525816662639087188L;

	public TooManyRequestsException(Request request) {
		this(request.toString(), request);
	}

	public TooManyRequestsException(String msg, Request request) {
		super(msg, request);
	}

}
