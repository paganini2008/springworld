package com.github.paganini2008.springworld.jobswarm;

/**
 * 
 * IllegalJobStateException
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class IllegalJobStateException extends JobException {

	private static final long serialVersionUID = 2500171134294863349L;

	public IllegalJobStateException(JobKey jobKey) {
		super(jobKey.toString());
	}

}
