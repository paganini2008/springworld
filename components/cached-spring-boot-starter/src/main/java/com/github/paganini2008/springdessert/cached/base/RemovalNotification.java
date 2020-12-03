package com.github.paganini2008.springdessert.cached.base;

import java.io.Serializable;

/**
 * 
 * RemovalNotification
 *
 * @author Jimmy Hoff
 * @since 1.0
 */
public class RemovalNotification implements Serializable {

	private static final long serialVersionUID = -2834343189063743330L;
	private String key;
	private Object value;
	private RemovalReason removalReason;

	public RemovalNotification(String key, Object value, RemovalReason removalReason) {
		this.key = key;
		this.value = value;
		this.removalReason = removalReason;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public RemovalReason getRemovalReason() {
		return removalReason;
	}

	public void setRemovalReason(RemovalReason removalReason) {
		this.removalReason = removalReason;
	}

}
