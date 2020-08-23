package com.github.paganini2008.springworld.myjob;

import java.util.Date;

import org.springframework.core.Ordered;

/**
 * 
 * JobListener
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public interface JobListener extends Ordered {

	default void beforeRun(JobKey jobKey, Date startDate) {
	}

	default void afterRun(JobKey jobKey, Date startDate, RunningState runningState, Throwable reason) {
	}

	default int getOrder() {
		return LOWEST_PRECEDENCE;
	}

}
