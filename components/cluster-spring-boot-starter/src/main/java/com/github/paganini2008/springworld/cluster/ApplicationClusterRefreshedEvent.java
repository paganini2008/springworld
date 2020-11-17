package com.github.paganini2008.springworld.cluster;

import org.springframework.context.ApplicationContext;

/**
 * 
 * ApplicationClusterRefreshedEvent
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class ApplicationClusterRefreshedEvent extends ApplicationClusterEvent {

	private static final long serialVersionUID = 3115067071903624457L;

	public ApplicationClusterRefreshedEvent(ApplicationContext applicationContext, ApplicationInfo leaderInfo) {
		super(applicationContext, ClusterState.ACCESSABLE);
		this.leaderInfo = leaderInfo;
	}

	private final ApplicationInfo leaderInfo;

	public ApplicationInfo getLeaderInfo() {
		return leaderInfo;
	}

}
