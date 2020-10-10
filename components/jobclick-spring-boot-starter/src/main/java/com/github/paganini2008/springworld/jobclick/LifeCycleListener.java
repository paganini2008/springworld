package com.github.paganini2008.springworld.jobclick;

import org.springframework.core.Ordered;

/**
 * 
 * LifeCycleListener
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public interface LifeCycleListener extends Ordered, Comparable<LifeCycleListener> {

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
	default int compareTo(LifeCycleListener other) {
		return other.getOrder() - getOrder();
	}

}
