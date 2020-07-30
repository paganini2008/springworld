package com.github.paganini2008.springworld.scheduler;

/**
 * 
 * JobCancelledException
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class JobCancelledException extends JobException {

	private static final long serialVersionUID = 5983515557191403926L;
	
	public JobCancelledException() {
	}

	public JobCancelledException(String msg) {
		super(msg);
	}

}
