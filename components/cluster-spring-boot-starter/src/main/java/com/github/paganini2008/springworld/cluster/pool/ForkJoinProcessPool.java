package com.github.paganini2008.springworld.cluster.pool;

/**
 * 
 * ForkJoinProcessPool
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public interface ForkJoinProcessPool {

	<T> T submit(ForkJoinProcess<T> work) throws Exception;

	void close();

}