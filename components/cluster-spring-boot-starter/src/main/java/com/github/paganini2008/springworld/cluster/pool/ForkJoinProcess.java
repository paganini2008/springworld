package com.github.paganini2008.springworld.cluster.pool;

/**
 * 
 * ForkJoinProcess
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public interface ForkJoinProcess<T> {

	T process(ForkJoinFrame<T> frame);

}
