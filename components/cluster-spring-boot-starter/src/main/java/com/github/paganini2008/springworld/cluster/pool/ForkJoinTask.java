package com.github.paganini2008.springworld.cluster.pool;

/**
 * 
 * ForkJoinTask
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public interface ForkJoinTask<T> {

	ForkJoinProcessTask<T> fork(ForkJoinProcess<T> subProcess);

	T call(Object... arguments);

}