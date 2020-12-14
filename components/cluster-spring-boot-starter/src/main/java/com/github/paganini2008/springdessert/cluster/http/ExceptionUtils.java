package com.github.paganini2008.springdessert.cluster.http;

import org.springframework.web.client.RestClientException;

/**
 * 
 * ExceptionUtils
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
public abstract class ExceptionUtils {

	public static RestfulException wrapException(String msg, Throwable e, Request request) {
		return wrapException(msg, e, request, InterruptedType.INTERNAL_ERROR);
	}

	public static RestfulException wrapException(String msg, Throwable e, Request request, InterruptedType interruptedType) {
		if (e instanceof RestClientException) {
			throw (RestClientException) e;
		}
		throw new RestfulException(msg, e, request, interruptedType);
	}

}
