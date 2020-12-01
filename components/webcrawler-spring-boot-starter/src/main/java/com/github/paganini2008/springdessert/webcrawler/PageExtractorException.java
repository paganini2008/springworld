package com.github.paganini2008.springdessert.webcrawler;

import org.springframework.http.HttpStatus;

/**
 * 
 * PageExtractorException
 *
 * @author Fred Feng
 * @since 1.0
 */
public class PageExtractorException extends RuntimeException {

	private static final long serialVersionUID = 4816595505153970862L;

	public PageExtractorException(String url, HttpStatus httpStatus) {
		super(url);
		this.httpStatus = httpStatus;
	}

	private final HttpStatus httpStatus;

	public HttpStatus getHttpStatus() {
		return httpStatus;
	}

}
