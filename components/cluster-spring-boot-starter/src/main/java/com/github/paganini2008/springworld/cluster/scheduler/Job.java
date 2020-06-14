package com.github.paganini2008.springworld.cluster.scheduler;

/**
 * 
 * Job
 *
 * @author Fred Feng
 * @version 1.0
 */
public interface Job {

	default String getName() {
		String simpleName = getClass().getSimpleName();
		return simpleName.substring(0, 1).toLowerCase().concat(simpleName.substring(1));
	}

	default String getJobClassName() {
		return getClass().getName();
	}

	default String getDescription() {
		return "";
	}

	default void onStart() {
	}

	Object execute();

	default void onSuccess(Object result) {
	}

	default void onFailure(Throwable e) {
	}

	default void onEnd() {
	}

	default int retries() {
		return 0;
	}

	default boolean isPersistent() {
		return true;
	}

}
