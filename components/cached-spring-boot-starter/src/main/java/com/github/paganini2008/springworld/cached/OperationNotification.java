package com.github.paganini2008.springworld.cached;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 * OperationNotification
 *
 * @author Fred Feng
 * @since 1.0
 */
@Setter
@Getter
public class OperationNotification implements Serializable {

	private static final long serialVersionUID = -3145872764240760266L;
	private String signature;
	private String key;
	private String name;
	private Object value;
	private Object returnResult;
	
	public OperationNotification() {
	}
	
	public OperationNotification(String signature,String key) {
		this(signature, key, null);
	}

	public OperationNotification(String signature, String key, Object value) {
		this(signature, key, null, value);
	}

	public OperationNotification(String signature, String key, String name, Object value) {
		this.signature = signature;
		this.key = key;
		this.name = name;
		this.value = value;
	}

}
