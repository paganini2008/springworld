package com.github.paganini2008.springworld.scheduler;

/**
 * 
 * JobCancellationException
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class JobCancellationException extends JobException {

	private static final long serialVersionUID = 5983515557191403926L;

	public JobCancellationException() {
	}

	public JobCancellationException(String msg) {
		super(msg);
	}

	public JobCancellationException(String msg, Throwable target) {
		super(msg, target);
	}

}
