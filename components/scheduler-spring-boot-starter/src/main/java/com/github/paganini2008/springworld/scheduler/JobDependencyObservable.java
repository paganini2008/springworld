package com.github.paganini2008.springworld.scheduler;

/**
 * 
 * JobDependencyObservable
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public interface JobDependencyObservable {

	JobFuture addDependency(Job job, String[] dependencies);

	void executeDependency(JobKey jobKey, Object attachment);

	void notifyDependants(JobKey jobKey, Object result);

}