package com.github.paganini2008.springworld.crontab;

/**
 * 
 * JobExecutor
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public interface JobExecutor {

	void execute(Job job, Object attachment);
	
	void addListener(JobRuntimeListener listener);
	
	void removeListener(JobRuntimeListener listener);

}