package com.github.paganini2008.springworld.scheduler;

import java.util.Date;

/**
 * 
 * JobRuntime
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public interface JobRuntime {
	
	String getJobName();

	JobState getJobState();
	
	RunningState getRunningState();

	Date getLastExecutionTime();

	Date getLastCompletionTime();

	Date getNextExecutionTime();

	long getCompletedCount();

	long getFailedCount();

	long getSkippedCount();

	default long getTotalCount() {
		return getCompletedCount() + getFailedCount() + getSkippedCount();
	}
	
}
