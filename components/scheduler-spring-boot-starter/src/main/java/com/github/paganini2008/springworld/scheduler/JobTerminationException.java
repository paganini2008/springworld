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

	public JobTerminationException(Job job) {
		super();
		this.job = job;
	}

	public JobTerminationException(Job job, Throwable e) {
		super(e.getMessage(), e);
		this.job = job;
	}

	private final Job job;

	public Job getJob() {
		return job;
	}

}
