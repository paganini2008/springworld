package com.github.paganini2008.springworld.cluster.multicast;

import org.springframework.context.ApplicationContext;

import com.github.paganini2008.springworld.cluster.ApplicationClusterEvent;
import com.github.paganini2008.springworld.cluster.ApplicationInfo;

/**
 * 
 * ClusterStateChangeEvent
 *
 * @author Fred Feng
 * @since 1.0
 */
public class ClusterStateChangeEvent extends ApplicationClusterEvent {

	private static final long serialVersionUID = -2482108960259276628L;

	public ClusterStateChangeEvent(ApplicationContext source, ApplicationInfo applicationInfo, EventType eventType) {
		super(source);
		this.applicationInfo = applicationInfo;
		this.eventType = eventType;
	}

	private final ApplicationInfo applicationInfo;
	private final EventType eventType;
	private Object message;

	public Object getMessage() {
		return message;
	}

	public void setMessage(Object message) {
		this.message = message;
	}

	public ApplicationInfo getApplicationInfo() {
		return applicationInfo;
	}

	public EventType getEventType() {
		return eventType;
	}

	public static enum EventType {
		ON_ACTIVE, ON_INACTIVE, ON_MESSAGE;
	}

}
