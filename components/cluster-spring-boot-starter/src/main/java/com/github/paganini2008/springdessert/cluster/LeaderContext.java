package com.github.paganini2008.springdessert.cluster;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.SmartApplicationListener;

/**
 * 
 * LeaderContext
 * 
 * @author Jimmy Hoff
 *
 * @since 1.0
 */
public class LeaderContext implements SmartApplicationListener {

	private ApplicationInfo leaderInfo;
	private volatile HealthState healthState = HealthState.UNKOWN;

	public ApplicationInfo getLeader() {
		return leaderInfo;
	}

	public HealthState getHealthState() {
		return healthState;
	}

	public void setHealthState(HealthState clusterState) {
		if (healthState.compareTo(HealthState.FATAL) < 0) {
			this.healthState = healthState;
		}
	}

	@Override
	public void onApplicationEvent(ApplicationEvent event) {
		ApplicationClusterEvent applicationClusterEvent = (ApplicationClusterEvent) event;
		this.healthState = applicationClusterEvent.getHealthState();

		if (event instanceof ApplicationClusterRefreshedEvent) {
			this.leaderInfo = ((ApplicationClusterRefreshedEvent) event).getLeaderInfo();
		}
	}

	@Override
	public boolean supportsEventType(Class<? extends ApplicationEvent> eventType) {
		return eventType == ApplicationClusterRefreshedEvent.class || eventType == ApplicationClusterFatalEvent.class;
	}

}
