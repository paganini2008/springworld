package com.github.paganini2008.springworld.crontab;

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

	public JobManagerEvent(JobKey jobKey, JobAction jobAction) {
		super(jobKey);
		this.jobAction = jobAction;
	}

	private final JobAction jobAction;

	public JobAction getJobAction() {
		return jobAction;
	}

}
