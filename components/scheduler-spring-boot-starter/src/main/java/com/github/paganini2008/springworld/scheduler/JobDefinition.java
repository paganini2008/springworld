package com.github.paganini2008.springworld.scheduler;

/**
 * 
 * JobDefinition
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public interface JobDefinition {

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

	default boolean managedByApplicationContext() {
		return true;
	}

	default String[] getDependencies() {
		return new String[0];
	}
}
