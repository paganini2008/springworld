package com.github.paganini2008.springworld.jobstorm;

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

	public JobTerminationException(JobKey jobKey) {
		super();
		this.jobKey = jobKey;
	}

	public JobTerminationException(JobKey jobKey, String reason) {
		super(reason);
		this.jobKey = jobKey;
	}

	public JobTerminationException(JobKey jobKey, Throwable e) {
		super(e.getMessage(), e);
		this.jobKey = jobKey;
	}

	private final JobKey jobKey;

	public JobKey getJobKey() {
		return jobKey;
	}

}
