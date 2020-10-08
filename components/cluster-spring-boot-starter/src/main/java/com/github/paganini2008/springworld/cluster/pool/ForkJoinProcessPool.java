package com.github.paganini2008.springworld.cluster.pool;

import java.util.concurrent.Future;

/**
 * 
 * ForkJoinProcessPool
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public interface ForkJoinProcessPool extends ProcessPool {

	<T> Future<T> submit(String serviceName, ForkJoinProcess<T> process);

	<T> Future<T> submit(Signature signature, ForkJoinProcess<T> process);

}