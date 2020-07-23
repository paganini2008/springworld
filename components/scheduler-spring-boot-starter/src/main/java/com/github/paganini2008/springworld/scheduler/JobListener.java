package com.github.paganini2008.springworld.scheduler;

/**
 * 
 * JobListener
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public interface JobListener {

	void beforeRun(Job job);

	void afterRun(Job job, RunningState runningState);

}
