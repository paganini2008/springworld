package com.github.paganini2008.springworld.cached;

import org.springframework.context.ApplicationEvent;

import com.github.paganini2008.springworld.cached.base.Cache;

/**
 * 
 * OperationNotificationEvent
 *
 * @author Fred Feng
 * @since 1.0
 */
public class OperationNotificationEvent extends ApplicationEvent {

	private static final long serialVersionUID = -474744672800806045L;

	public OperationNotificationEvent(Cache cache, OperationNotification operationNotification) {
		super(cache);
		this.operationNotification = operationNotification;
	}

	private final OperationNotification operationNotification;

	public OperationNotification getOperationNotification() {
		return operationNotification;
	}

}
