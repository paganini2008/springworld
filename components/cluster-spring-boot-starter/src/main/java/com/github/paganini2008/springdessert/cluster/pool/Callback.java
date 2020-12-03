package com.github.paganini2008.springdessert.cluster.pool;

/**
 * 
 * Callback
 * 
 * @author Jimmy Hoff
 *
 * @since 1.0
 */
public interface Callback {
	
	Invocation getInvocation();

	String getMethodName();

	Object[] getArguments();

}
