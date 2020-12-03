package com.github.paganini2008.springdessert.jobsoup;

/**
 * 
 * LifeCycleListener
 * 
 * @author Jimmy Hoff
 *
 * @since 1.0
 */
public interface LifeCycleListener extends Comparable<LifeCycleListener> {

	default void afterCreation(JobKey jobKey) {
	}

	default void beforeDeletion(JobKey jobKey) {
	}

	default void afterRefresh(JobKey jobKey) {
	}

	default int getOrder() {
		return 0;
	}

	@Override
	default int compareTo(LifeCycleListener other) {
		return other.getOrder() - getOrder();
	}

}
