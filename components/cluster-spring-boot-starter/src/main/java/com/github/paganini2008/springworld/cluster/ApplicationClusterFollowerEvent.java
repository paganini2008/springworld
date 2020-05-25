package com.github.paganini2008.springworld.cluster;

import org.springframework.context.ApplicationContext;

/**
 * 
 * ApplicationClusterFollowerEvent
 * 
 * @author Fred Feng
 * @version 1.0
 */
public class ApplicationClusterFollowerEvent extends ApplicationClusterEvent {

	private static final long serialVersionUID = 9109166626001674260L;

	public ApplicationClusterFollowerEvent(ApplicationContext context, String leaderId) {
		super(context);
		this.leaderId = leaderId;
	}

	private final String leaderId;

	public String getLeaderId() {
		return leaderId;
	}

}
