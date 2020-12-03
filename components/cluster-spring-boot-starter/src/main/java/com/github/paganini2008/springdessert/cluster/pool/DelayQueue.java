package com.github.paganini2008.springdessert.cluster.pool;

/**
 * 
 * DelayQueue
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
public interface DelayQueue {

	void offer(Invocation invocation);

	Invocation pop();

	void waitForTermination();

	int size();

}