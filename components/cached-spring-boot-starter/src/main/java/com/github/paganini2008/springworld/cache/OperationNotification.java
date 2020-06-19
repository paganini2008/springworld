package com.github.paganini2008.springworld.cache;

import java.io.Serializable;

/**
 * 
 * OperationNotification
 *
 * @author Fred Feng
 * @since 1.0
 */
public class OperationNotification implements Serializable {

	private static final long serialVersionUID = -3145872764240760266L;
	private String signature;
	private String key;
	private String name;
	private Object value;
	
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

	public String getSignature() {
		return signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

}
