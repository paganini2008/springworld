package com.github.paganini2008.springworld.cluster.pool;

import java.io.Serializable;
import java.util.UUID;

import com.github.paganini2008.devtools.beans.ToStringBuilder;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 * Call
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Getter
@Setter
public class Call implements Signature, Serializable {

	private static final long serialVersionUID = -5401293046063974728L;

	private String id;
	private String beanName;
	private String beanClassName;
	private String methodName;
	private Object[] arguments;
	private long timestamp;

	public Call(String beanName, String beanClassName, String methodName, Object... arguments) {
		this.id = UUID.randomUUID().toString();
		this.beanName = beanName;
		this.beanClassName = beanClassName;
		this.methodName = methodName;
		this.arguments = arguments;
		this.timestamp = System.currentTimeMillis();
	}

	public Call() {
	}

	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

}
