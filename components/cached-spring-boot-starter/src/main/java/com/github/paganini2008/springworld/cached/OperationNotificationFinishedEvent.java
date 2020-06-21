package com.github.paganini2008.springworld.cached;

import com.github.paganini2008.springworld.cached.base.Cache;

/**
 * 
 * OperationNotificationFinishedEvent
 *
 * @author Fred Feng
 * @since 1.0
 */
public class OperationNotificationFinishedEvent extends OperationNotificationEvent {

	private static final long serialVersionUID = 6350293111545234200L;

	public OperationNotificationFinishedEvent(Cache cache, OperationNotification operationNotification) {
		super(cache, operationNotification);
	}

}
