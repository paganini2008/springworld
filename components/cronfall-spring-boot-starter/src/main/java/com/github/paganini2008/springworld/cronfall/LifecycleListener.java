package com.github.paganini2008.springworld.cronfall;

import org.springframework.core.Ordered;

/**
 * 
 * LifecycleListener
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public interface LifecycleListener extends Ordered, Comparable<LifecycleListener> {

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
	default int compareTo(LifecycleListener other) {
		return other.getOrder() - getOrder();
	}

}
