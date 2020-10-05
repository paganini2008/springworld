package com.github.paganini2008.springworld.cluster.pool;

/**
 * 
 * Signature
 *
 * @author Fred Feng
 * 
 * 
 * @version 1.0
 */
public interface Signature {
	
	String getId();
	
	String getBeanName();

	String getBeanClassName();
	
	String getMethodName();
	
	Object[] getArguments();
	
	long getTimestamp();
	
}
