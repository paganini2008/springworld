package com.github.paganini2008.springworld.cluster.consistency;

import org.springframework.context.ApplicationEvent;

/**
 * 
 * ConsistencyRequestConfirmationEvent
 *
 * @author Fred Feng
 * @since 1.0
 */
public class ConsistencyRequestConfirmationEvent extends ApplicationEvent {

	private static final long serialVersionUID = 4041272418956233610L;

	public ConsistencyRequestConfirmationEvent(ConsistencyRequest request, String instanceId, boolean ok) {
		super(request);
		this.instanceId = instanceId;
		this.ok = ok;
	}

	private final String instanceId;
	private final boolean ok;

	public String getInstanceId() {
		return instanceId;
	}

	public boolean isOk() {
		return ok;
	}

}
