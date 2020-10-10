package com.github.paganini2008.springworld.jobclick;

import java.util.Date;

import org.springframework.core.Ordered;

/**
 * 
 * JobRuntimeListener
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public interface JobRuntimeListener extends Ordered, Comparable<JobRuntimeListener> {

	default void beforeRun(long traceId, JobKey jobKey, Object attachment, Date startDate) {
	}

	default void afterRun(long traceId, JobKey jobKey, Object attachment, Date startDate, RunningState runningState, Object result,
			Throwable reason) {
	}

	default int getOrder() {
		return LOWEST_PRECEDENCE;
	}

	default int compareTo(JobRuntimeListener other) {
		return other.getOrder() - getOrder();
	}

}
