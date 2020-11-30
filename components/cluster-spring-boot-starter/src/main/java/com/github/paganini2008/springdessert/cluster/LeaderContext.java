package com.github.paganini2008.springdessert.cluster;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.SmartApplicationListener;

/**
 * 
 * LeaderContext
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class LeaderContext implements SmartApplicationListener {

	private ApplicationInfo leaderInfo;
	private volatile ClusterState clusterState = ClusterState.UNKOWN;

	public ApplicationInfo getLeader() {
		return leaderInfo;
	}

	public ClusterState getClusterState() {
		return clusterState;
	}

	public void setClusterState(ClusterState clusterState) {
		if (clusterState.compareTo(ClusterState.FATAL) < 0) {
			this.clusterState = clusterState;
		}
	}

	@Override
	public void onApplicationEvent(ApplicationEvent event) {
		ApplicationClusterEvent applicationClusterEvent = (ApplicationClusterEvent) event;
		this.clusterState = applicationClusterEvent.getClusterState();

		if (event instanceof ApplicationClusterRefreshedEvent) {
			this.leaderInfo = ((ApplicationClusterRefreshedEvent) event).getLeaderInfo();
		}
	}

	@Override
	public boolean supportsEventType(Class<? extends ApplicationEvent> eventType) {
		return eventType == ApplicationClusterRefreshedEvent.class || eventType == ApplicationClusterFatalEvent.class;
	}

}