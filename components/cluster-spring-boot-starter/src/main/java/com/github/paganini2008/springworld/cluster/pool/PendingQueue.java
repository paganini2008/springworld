package com.github.paganini2008.springworld.cluster.pool;

/**
 * 
 * PendingQueue
 *
 * @author Fred Feng
 * @version 1.0
 */
public interface PendingQueue {

	void set(Signature signature);

	Signature get();

	void waitForTermination();

	int size();

}