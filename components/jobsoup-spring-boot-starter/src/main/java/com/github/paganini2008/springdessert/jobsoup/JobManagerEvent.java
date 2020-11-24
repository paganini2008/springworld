package com.github.paganini2008.springdessert.jobsoup;

import org.springframework.context.ApplicationEvent;

/**
 * 
 * JobManagerEvent
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class JobManagerEvent extends ApplicationEvent {

	private static final long serialVersionUID = -5163299536045264598L;

	public JobManagerEvent(JobKey jobKey, JobLifeCycle jobAction) {
		super(jobKey);
		this.jobAction = jobAction;
	}

	private final JobLifeCycle jobAction;

	public JobLifeCycle getJobAction() {
		return jobAction;
	}

}
