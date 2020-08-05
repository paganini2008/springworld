package com.github.paganini2008.springworld.scheduler;

/**
 * 
 * JobTerminationException
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class JobTerminationException extends JobException {

	private static final long serialVersionUID = 7325304130493602160L;

	public JobTerminationException(String msg) {
		super(msg);
	}

	public JobTerminationException(String msg, Throwable e) {
		super(msg, e);
	}

}
