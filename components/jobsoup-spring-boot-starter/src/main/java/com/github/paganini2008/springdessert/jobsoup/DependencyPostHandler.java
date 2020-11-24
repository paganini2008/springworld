package com.github.paganini2008.springdessert.jobsoup;

/**
 * 
 * DependencyPostHandler
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public interface DependencyPostHandler {

	boolean approve(JobKey jobKey, RunningState runningState, Object attachment, Object result);

}
