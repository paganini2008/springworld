package com.github.paganini2008.springworld.scheduler;

/**
 * 
 * Job
 *
 * @author Fred Feng
 * @since 1.0
 */
public interface Job extends JobDefinition {
	
	default String getJobName() {
		String simpleName = getClass().getSimpleName();
		return simpleName.substring(0, 1).toLowerCase().concat(simpleName.substring(1));
	}

	default String getJobClassName() {
		return getClass().getName();
	}

	String getGroupName();

	default String getDescription() {
		return "";
	}

	default int getRetries() {
		return 0;
	}

	default String getAttachment() {
		return null;
	}

	default void onStart() {
	}

	default boolean onSuccess(Object result) {
		return true;
	}

	default boolean onFailure(Throwable e) {
		return true;
	}

	default void onEnd() {
	}

	default boolean shouldRun() {
		return true;
	}

	Object execute(Object result);

}
