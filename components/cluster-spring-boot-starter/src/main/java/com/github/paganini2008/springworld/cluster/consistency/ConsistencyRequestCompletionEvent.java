package com.github.paganini2008.springworld.cluster.consistency;

import org.springframework.context.ApplicationEvent;

/**
 * 
 * ConsistencyRequestCompletionEvent
 *
 * @author Fred Feng
 *
 * @since 1.0
 */
public class ConsistencyRequestCompletionEvent extends ApplicationEvent {

	private static final long serialVersionUID = -6200632827461240339L;

	public ConsistencyRequestCompletionEvent(ConsistencyRequest consistencyRequest, String instanceId) {
		super(consistencyRequest);
		this.instanceId = instanceId;
	}

	private final String instanceId;

	public String getInstanceId() {
		return instanceId;
	}

}
