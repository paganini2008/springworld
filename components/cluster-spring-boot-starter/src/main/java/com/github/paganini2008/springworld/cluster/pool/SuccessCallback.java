package com.github.paganini2008.springworld.cluster.pool;

/**
 * 
 * SuccessCallback
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class SuccessCallback extends Return {

	public SuccessCallback() {
	}

	SuccessCallback(Signature signature, Object returnValue, String methodName) {
		super(signature, returnValue);
		this.methodName = methodName;
	}

	private String methodName;

	@Override
	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

}
