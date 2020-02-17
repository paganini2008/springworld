package com.github.paganini2008.springworld.cluster;

import org.springframework.context.ApplicationContext;

/**
 * 
 * ContextSlaveStandbyEvent
 * 
 * @author Fred Feng
 * 
 * 
 * @version 1.0
 */
public class ContextSlaveStandbyEvent extends ContextClusterEvent {

	private static final long serialVersionUID = 9109166626001674260L;

	public ContextSlaveStandbyEvent(ApplicationContext context) {
		super(context);
	}

}
