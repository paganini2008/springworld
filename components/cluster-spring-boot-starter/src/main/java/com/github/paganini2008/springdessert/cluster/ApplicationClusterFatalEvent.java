package com.github.paganini2008.springdessert.cluster;

import org.springframework.context.ApplicationContext;

/**
 * 
 * ApplicationClusterFatalEvent
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class ApplicationClusterFatalEvent extends ApplicationClusterEvent {

	private static final long serialVersionUID = -922319097605054253L;

	public ApplicationClusterFatalEvent(ApplicationContext source, Throwable reason) {
		super(source, ClusterState.FATAL);
		this.reason = reason;
	}

	private final Throwable reason;

	public Throwable getReason() {
		return reason;
	}

}
