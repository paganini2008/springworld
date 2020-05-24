package com.github.paganini2008.springworld.cluster;

import org.springframework.context.ApplicationContext;

/**
 * 
 * ApplicationClusterLeaderStandbyEvent
 * 
 * @author Fred Feng
 * @version 1.0
 */
public class ApplicationClusterLeaderStandbyEvent extends ApplicationClusterEvent {

	private static final long serialVersionUID = -2932470508571995512L;

	public ApplicationClusterLeaderStandbyEvent(ApplicationContext context) {
		super(context);
	}

}
