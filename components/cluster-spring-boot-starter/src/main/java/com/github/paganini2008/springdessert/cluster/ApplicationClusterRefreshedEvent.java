package com.github.paganini2008.springdessert.cluster;

import org.springframework.context.ApplicationContext;

/**
 * 
 * ApplicationClusterRefreshedEvent
 * 
 * @author Jimmy Hoff
 *
 * @since 1.0
 */
public class ApplicationClusterRefreshedEvent extends ApplicationClusterEvent {

	private static final long serialVersionUID = 3115067071903624457L;

	public ApplicationClusterRefreshedEvent(ApplicationContext applicationContext, ApplicationInfo leader) {
		super(applicationContext, HealthState.LEADABLE);
		this.leader = leader;
	}

	private final ApplicationInfo leader;

	public ApplicationInfo getLeaderInfo() {
		return leader;
	}

}
