package com.github.paganini2008.springworld.cluster.pool;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
public class FailureCallback extends Return {

	public FailureCallback() {
	}

	private String methodName;
	private Throwable reason;

	FailureCallback(Signature signature, Throwable reason, String methodName) {
		super(signature, null);
		this.reason = reason;
		this.methodName = methodName;
	}

	@Override
	public String getMethodName() {
		return methodName;
	}

	@JsonIgnore
	@Override
	public Object[] getArguments() {
		return new Object[] { reason, getSignature() };
	}

}
