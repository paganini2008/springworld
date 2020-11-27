package com.github.paganini2008.springdessert.cluster.consistency;

import org.springframework.context.ApplicationEvent;

import com.github.paganini2008.springdessert.cluster.ApplicationInfo;

/**
 * 
 * ConsistencyRequestCompletionEvent
 *
 * @author Fred Feng
 *
 * @since 1.0
 */
public class ConsistencyRequestCompletionEvent extends ApplicationEvent {

	private static final long serialVersionUID = -6200632827461240339L;

	public ConsistencyRequestCompletionEvent(ConsistencyRequest consistencyRequest, ApplicationInfo applicationInfo) {
		super(consistencyRequest);
		this.applicationInfo = applicationInfo;
	}

	private final ApplicationInfo applicationInfo;

	public ApplicationInfo getApplicationInfo() {
		return applicationInfo;
	}

}
