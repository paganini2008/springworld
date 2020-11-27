package com.github.paganini2008.springdessert.cluster.pool;

/**
 * 
 * Callback
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public interface Callback {
	
	Invocation getInvocation();

	String getMethodName();

	Object[] getArguments();

}
