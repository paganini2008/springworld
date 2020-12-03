package com.github.paganini2008.springdessert.jobsoup;

import java.util.Date;

/**
 * 
 * JobRuntimeListener
 * 
 * @author Jimmy Hoff
 *
 * @since 1.0
 */
public interface JobRuntimeListener extends Comparable<JobRuntimeListener> {

	default void beforeRun(long traceId, JobKey jobKey, Object attachment, Date startDate) {
	}

	default void afterRun(long traceId, JobKey jobKey, Object attachment, Date startDate, RunningState runningState, Object result,
			Throwable reason) {
	}

	default int getOrder() {
		return 0;
	}

	default int compareTo(JobRuntimeListener other) {
		return other.getOrder() - getOrder();
	}

}
