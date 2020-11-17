package com.github.paganini2008.springworld.cluster;

import org.springframework.context.ApplicationContext;

/**
 * 
 * ApplicationClusterNewLeaderEvent
 * 
 * @author Fred Feng
 * @version 1.0
 */
public class ApplicationClusterNewLeaderEvent extends ApplicationClusterEvent {

	private static final long serialVersionUID = -2932470508571995512L;

	public ApplicationClusterNewLeaderEvent(ApplicationContext context) {
		super(context, ClusterState.ACCESSABLE);
	}

}
