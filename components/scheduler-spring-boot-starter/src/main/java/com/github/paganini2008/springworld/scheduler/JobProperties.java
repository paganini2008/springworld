package com.github.paganini2008.springworld.scheduler;

/**
 * 
 * JobProperties
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public interface JobProperties {

	default String getJobName() {
		String simpleName = getClass().getSimpleName();
		return simpleName.substring(0, 1).toLowerCase().concat(simpleName.substring(1));
	}

	default String getJobClassName() {
		return getClass().getName();
	}

	String getGroupName();

	Trigger getTrigger();

	default String getDescription() {
		return "";
	}

	default int getRetries() {
		return 0;
	}

	default String getEmail() {
		return "";
	}

	default boolean managedByApplicationContext() {
		return true;
	}
}
