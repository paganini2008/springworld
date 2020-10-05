package com.github.paganini2008.springworld.cluster.pool;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 * FailureCallback
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Setter
@Getter
public class FailureCallback implements Callback {

	public FailureCallback() {
	}

	private String methodName;
	private Throwable reason;
	private Signature signature;

	FailureCallback(String methodName, Throwable reason, Signature signature) {
		this.methodName = methodName;
		this.reason = reason;
		this.signature = signature;
	}

	@Override
	public String getId() {
		return signature.getId();
	}

	@Override
	public String getBeanName() {
		return signature.getBeanName();
	}

	@Override
	public String getBeanClassName() {
		return signature.getBeanClassName();
	}

	@Override
	public String getMethodName() {
		return methodName;
	}

	@Override
	public Object[] getArguments() {
		return new Object[] { reason };
	}

	@Override
	public long getTimestamp() {
		return signature.getTimestamp();
	}

}
