package com.github.paganini2008.springworld.reditools.common;

import java.util.concurrent.TimeUnit;

/**
 * 
 * DistributedCountDownLatch
 * 
 * @author Fred Feng
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
	 * @author Fred Feng
	 *
	 * @since 1.0
	 */
	public interface InterruptibleHandler {

		void onCancellation();

		void onTimeout();

	}

}