package com.github.paganini2008.springworld.jobstorm;

/**
 * 
 * DependencyHandler
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public interface DependencyHandler {

	boolean approve(JobKey jobKey, RunningState runningState, Object attachment, Object result);

}
