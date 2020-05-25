package com.github.paganini2008.springworld.cluster.consistency;

import org.springframework.context.ApplicationEvent;

/**
 * 
 * ConsistencyOperationResult
 *
 * @author Fred Feng
 * @since 1.0
 */
public class ConsistencyOperationResult extends ApplicationEvent {

	private static final long serialVersionUID = 4041272418956233610L;

	public ConsistencyOperationResult(String name, Object value) {
		super(name);
		this.value = value;
	}

	private final Object value;

	public String getName() {
		return (String) getSource();
	}

	public Object getValue() {
		return value;
	}

}
