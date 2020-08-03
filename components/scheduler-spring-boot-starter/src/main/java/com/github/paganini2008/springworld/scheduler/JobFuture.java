package com.github.paganini2008.springworld.scheduler;

/**
 * 
 * JobFuture
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public interface JobFuture {

	void cancel();

	boolean isDone();

	boolean isCancelled();

	long getNextExectionTime();

}
