package com.github.paganini2008.springworld.cluster.multicast;

import org.springframework.context.ApplicationContext;

import com.github.paganini2008.springworld.cluster.ApplicationClusterEvent;

/**
 * 
 * ClusterStateChangeEvent
 *
 * @author Fred Feng
 * @since 1.0
 */
public class ClusterStateChangeEvent extends ApplicationClusterEvent {

	private static final long serialVersionUID = -2482108960259276628L;

	public ClusterStateChangeEvent(ApplicationContext source, String instanceId, EventType eventType) {
		super(source);
		this.instanceId = instanceId;
		this.eventType = eventType;
	}

	private final String instanceId;
	private final EventType eventType;
	private Object message;

	public Object getMessage() {
		return message;
	}

	public void setMessage(Object message) {
		this.message = message;
	}

	public String getInstanceId() {
		return instanceId;
	}

	public EventType getEventType() {
		return eventType;
	}

	public static enum EventType {
		ON_ACTIVE, ON_INACTIVE, ON_MESSAGE;
	}

}
