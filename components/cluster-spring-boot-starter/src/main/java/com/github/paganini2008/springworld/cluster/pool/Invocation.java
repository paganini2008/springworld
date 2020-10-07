package com.github.paganini2008.springworld.cluster.pool;

/**
 * 
 * Invocation
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public interface Invocation {

	String getId();

	Signature getSignature();

	Object[] getArguments();

	long getTimestamp();

}