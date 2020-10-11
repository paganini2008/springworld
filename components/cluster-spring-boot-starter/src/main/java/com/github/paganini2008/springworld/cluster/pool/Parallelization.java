package com.github.paganini2008.springworld.cluster.pool;

/**
 * 
 * CallParallelization
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public interface Parallelization {

	Object[] slice(Object argument);

	Object merge(Object[] results);

}