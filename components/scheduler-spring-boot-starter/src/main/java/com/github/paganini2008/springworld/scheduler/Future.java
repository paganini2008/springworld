package com.github.paganini2008.springworld.scheduler;

/**
 * 
 * Future
 * @author Fred Feng
 *
 * @since 1.0
 */
public interface Future {

	void cancel();
	
	boolean isDone();
	
	long getNextExectionTime();
	
}
