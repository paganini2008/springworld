package com.github.paganini2008.springdessert.cluster;

import org.springframework.context.ApplicationContext;

/**
 * 
 * ApplicationClusterFollowerEvent
 * 
 * @author Jimmy Hoff
 * @version 1.0
 */
public class ApplicationClusterFollowerEvent extends ApplicationClusterEvent {

	private static final long serialVersionUID = 9109166626001674260L;

	public ApplicationClusterFollowerEvent(ApplicationContext context, ApplicationInfo leaderInfo) {
		super(context, ClusterState.ACCESSABLE);
		this.leaderInfo = leaderInfo;
	}

	private final ApplicationInfo leaderInfo;

	public ApplicationInfo getLeaderInfo() {
		return leaderInfo;
	}

}
