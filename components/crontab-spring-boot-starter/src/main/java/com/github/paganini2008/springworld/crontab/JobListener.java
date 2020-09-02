package com.github.paganini2008.springworld.crontab;

import org.springframework.core.Ordered;

/**
 * 
 * JobListener
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public interface JobListener extends Ordered, Comparable<JobListener> {

	default void afterCreation(JobKey jobKey) {
	}

	default void beforeDeletion(JobKey jobKey) {
	}

	default void afterRefresh(JobKey jobKey) {
	}

	default int getOrder() {
		return LOWEST_PRECEDENCE;
	}

	@Override
	default int compareTo(JobListener other) {
		return other.getOrder() - getOrder();
	}

}
