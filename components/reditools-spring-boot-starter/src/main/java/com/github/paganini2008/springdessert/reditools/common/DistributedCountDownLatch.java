package com.github.paganini2008.springdessert.reditools.common;

import java.util.concurrent.TimeUnit;

/**
 * 
 * DistributedCountDownLatch
 * 
 * @author Jimmy Hoff
 *
 * @since 1.0
 */
public interface DistributedCountDownLatch {

	void countdown(Object attachment);

	void countdown(int permits, Object attachment);

	Object[] await(int permits, InterruptibleHandler handler);

	Object[] await(int permits, long timeout, TimeUnit timeUnit, InterruptibleHandler handler);

	void cancel();

	/**
	 * 
	 * InterruptibleHandler
	 * 
	 * @author Jimmy Hoff
	 *
	 * @since 1.0
	 */
	public interface InterruptibleHandler {

		void onCancellation();

		void onTimeout();

	}

}