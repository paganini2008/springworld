package com.github.paganini2008.springworld.cluster.pool;

import java.util.concurrent.TimeUnit;

/**
 * 
 * TaskPromise
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public interface TaskPromise {

	Object get();

	Object get(long timeout, TimeUnit timeUnit);

	void cancel();

	boolean isCancelled();

	boolean isDone();

}
