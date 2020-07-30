package com.github.paganini2008.springworld.scheduler;

/**
 * 
 * JobLoadingCallback
 *
 * @author Fred Feng
 */
public interface JobLoadingCallback {

	void postLoad(Job job, Object attachment);

}
