package com.github.paganini2008.springworld.cluster.pool;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 * SuccessCallback
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Setter
@Getter
public class SuccessCallback implements Callback {

	private String methodName;
	private Object argument;
	private Signature signature;

	public SuccessCallback() {
	}

	SuccessCallback(String methodName, Object argument, Signature signature) {
		this.methodName = methodName;
		this.argument = argument;
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
		return new Object[] { argument, signature };
	}

	@Override
	public long getTimestamp() {
		return signature.getTimestamp();
	}

}
