package com.github.paganini2008.springworld.crontab;

/**
 * 
 * JobDependencyObservable
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public interface JobDependencyObservable {

	JobFuture addDependency(Job job, JobKey... dependencies);

	void executeDependency(JobKey jobKey, Object attachment);

	void notifyDependants(JobKey jobKey, Object result);

}