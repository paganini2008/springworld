package com.github.paganini2008.springworld.myjob;

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
	
	void addJobListener(JobListener listener);
	
	void removeJobListener(JobListener listener);

}
