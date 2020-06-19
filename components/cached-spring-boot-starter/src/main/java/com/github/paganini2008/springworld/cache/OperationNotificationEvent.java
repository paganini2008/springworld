package com.github.paganini2008.springworld.cache;

import org.springframework.context.ApplicationEvent;

/**
 * 
 * OperationNotificationEvent
 *
 * @author Fred Feng
 * @since 1.0
 */
public class OperationNotificationEvent extends ApplicationEvent {

	private static final long serialVersionUID = -6400039183325187092L;

	public OperationNotificationEvent(Cache cache, OperationNotification operationNotification) {
		super(cache);
		this.operationNotification = operationNotification;
	}

	private final OperationNotification operationNotification;

	public OperationNotification getOperationNotification() {
		return operationNotification;
	}

}
