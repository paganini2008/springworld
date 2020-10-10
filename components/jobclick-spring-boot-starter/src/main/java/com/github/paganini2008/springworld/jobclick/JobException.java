package com.github.paganini2008.springworld.jobclick;

/**
 * 
 * JobException
 *
 * @author Fred Feng
 * @since 1.0
 */
public class JobException extends RuntimeException {

	private static final long serialVersionUID = -7523610934014132232L;

	public JobException() {
		super();
	}

	public JobException(String msg) {
		super(msg);
	}

	public JobException(String msg, Throwable e) {
		super(msg, e);
	}

}
