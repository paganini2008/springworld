package com.github.paganini2008.springdessert.cluster.http;

import org.springframework.http.HttpStatus;

/**
 * 
 * InterruptedType
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
public enum InterruptedType {

	REQUEST_TIMEOUT(HttpStatus.REQUEST_TIMEOUT), INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR),
	TOO_MANY_REQUESTS(HttpStatus.TOO_MANY_REQUESTS);

	private final HttpStatus httpStatus;

	private InterruptedType(HttpStatus httpStatus) {
		this.httpStatus = httpStatus;
	}

	public HttpStatus getHttpStatus() {
		return httpStatus;
	}

}
