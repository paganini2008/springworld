package com.github.paganini2008.springworld.myjob;

/**
 * 
 * NotManagedJob
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public interface NotManagedJob {

	default void prepare(JobKey jobKey) {
	}

	default void onSuccess(JobKey jobKey, Object result) {
	}

	default void onFailure(JobKey jobKey, Throwable e) {
	}

	default boolean shouldRun(JobKey jobKey) {
		return true;
	}

	Object execute(JobKey jobKey, Object result);
	
}
