package com.github.paganini2008.springworld.jobclick;

import org.slf4j.Logger;

/**
 * 
 * NotManagedJob
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public interface NotManagedJob {

	default void prepare(JobKey jobKey, Logger log) {
	}

	default void onSuccess(JobKey jobKey, Object result, Logger log) {
	}

	default void onFailure(JobKey jobKey, Throwable e, Logger log) {
	}

	default boolean shouldRun(JobKey jobKey, Logger log) {
		return true;
	}

	Object execute(JobKey jobKey, Object attachment, Logger log) throws Exception;

}
