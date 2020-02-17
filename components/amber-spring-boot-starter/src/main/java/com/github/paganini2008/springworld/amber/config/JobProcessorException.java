package com.github.paganini2008.springworld.amber.config;

/**
 * 
 * JobProcessorException
 * 
 * @author Fred Feng
 * 
 */
public class JobProcessorException extends RuntimeException {

	private static final long serialVersionUID = 7593596680424549258L;

	public JobProcessorException(String msg) {
		super(msg);
	}

	public JobProcessorException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
