package com.github.paganini2008.springworld.jobsoup;

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
		super();
		this.jobKey = jobKey;
	}

	public IllegalJobStateException(JobKey jobKey, String msg) {
		super(msg);
		this.jobKey = jobKey;
	}

	private final JobKey jobKey;

	public JobKey getJobKey() {
		return jobKey;
	}

}
