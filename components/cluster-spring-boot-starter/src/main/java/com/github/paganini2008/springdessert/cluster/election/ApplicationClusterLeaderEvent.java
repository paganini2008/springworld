package com.github.paganini2008.springdessert.cluster.election;

import org.springframework.context.ApplicationContext;

import com.github.paganini2008.springdessert.cluster.ApplicationClusterEvent;
import com.github.paganini2008.springdessert.cluster.HealthState;

/**
 * 
 * ApplicationClusterLeaderEvent
 * 
 * @author Jimmy Hoff
 * @version 1.0
 */
public class ApplicationClusterLeaderEvent extends ApplicationClusterEvent {

	private static final long serialVersionUID = -2932470508571995512L;

	public ApplicationClusterLeaderEvent(ApplicationContext context) {
		super(context, HealthState.LEADABLE);
	}

}
