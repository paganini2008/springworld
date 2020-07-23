package com.github.paganini2008.springworld.scheduler;

/**
 * 
 * JobLoadingCallback
 *
 * @author Fred Feng
 */
public interface JobLoadingCallback {

	void afterLoad(Job job);

}
