package com.github.paganini2008.springworld.scheduler;

/**
 * 
 * Job
 *
 * @author Fred Feng
 * @since 1.0
 */
public interface Job {

	default String getJobName() {
		String simpleName = getClass().getSimpleName();
		return simpleName.substring(0, 1).toLowerCase().concat(simpleName.substring(1));
	}

	default String getJobClassName() {
		return getClass().getName();
	}

	default String getSignature() {
		return getJobName() + "@" + getJobClassName();
	}

	default String getDescription() {
		return "";
	}

	default int getRetries() {
		return 0;
	}

	default Object getAttachment() {
		return null;
	}

	default void onStart() {
	}

	default void onSuccess(Object result) {
	}

	default void onFailure(Throwable e) {
	}

	default void onEnd() {
	}

	Object execute(Object result);

}
