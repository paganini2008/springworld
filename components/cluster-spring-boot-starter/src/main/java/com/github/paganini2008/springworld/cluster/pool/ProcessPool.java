package com.github.paganini2008.springworld.cluster.pool;

/**
 * 
 * ProcessPool
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public interface ProcessPool {

	void execute(Invocation invocation);

	TaskPromise submit(Invocation invocation);

	TaskPromise submit(String identifier, Object... arguments);

	int getQueueSize();

	void shutdown();

}
