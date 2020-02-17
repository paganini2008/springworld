package com.github.paganini2008.springworld.cluster.pool;

/**
 * 
 * WorkQueue
 *
 * @author Fred Feng
 * 
 * 
 * @version 1.0
 */
public interface WorkQueue {

	void push(Signature signature);

	Signature pop();

	void waitForTermination();

	int size();

}