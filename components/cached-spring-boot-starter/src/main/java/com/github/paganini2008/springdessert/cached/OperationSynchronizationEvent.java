package com.github.paganini2008.springdessert.cached;

import org.springframework.context.ApplicationEvent;

/**
 * 
 * OperationSynchronizationEvent
 *
 * @author Fred Feng
 * @since 1.0
 */
public class OperationSynchronizationEvent extends ApplicationEvent {

	private static final long serialVersionUID = 6381584414940930864L;

	public OperationSynchronizationEvent(OperationNotification operationNotification) {
		super(operationNotification);
	}

	public OperationNotification getSource() {
		return (OperationNotification) super.getSource();
	}

}
