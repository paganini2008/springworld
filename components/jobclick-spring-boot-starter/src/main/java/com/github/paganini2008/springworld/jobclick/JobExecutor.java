package com.github.paganini2008.springworld.jobclick;

/**
 * 
 * JobExecutor
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public interface JobExecutor {

	void execute(Job job, Object attachment, int retries);

	void addListener(JobKey jobKey, JobRuntimeListener listener);

	void removeListener(JobKey jobKey, JobRuntimeListener listener);

}
