package com.github.paganini2008.springworld.cluster.pool;

/**
 * 
 * ParallelizingPolicy
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public interface ParallelizingPolicy {

	Object[] slice(Object argument);

	Object merge(Object[] results);

}
