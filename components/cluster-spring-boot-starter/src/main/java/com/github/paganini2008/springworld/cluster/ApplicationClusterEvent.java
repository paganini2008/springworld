package com.github.paganini2008.springworld.cluster;

import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ApplicationContextEvent;

/**
 * 
 * Base class for cluster event
 *
 * @author Fred Feng
 * @version 1.0
 */
public abstract class ApplicationClusterEvent extends ApplicationContextEvent {

	private static final long serialVersionUID = -9030425105386583374L;

	public ApplicationClusterEvent(ApplicationContext source) {
		super(source);
	}

}
