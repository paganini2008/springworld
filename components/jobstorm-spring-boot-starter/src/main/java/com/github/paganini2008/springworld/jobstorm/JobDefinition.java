package com.github.paganini2008.springworld.jobstorm;

/**
 * 
 * JobDefinition
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public interface JobDefinition {

	String getClusterName();

	String getGroupName();

	default String getJobName() {
		String simpleName = getClass().getSimpleName();
		return simpleName.substring(0, 1).toLowerCase().concat(simpleName.substring(1));
	}

	default String getJobClassName() {
		return getClass().getName();
	}

	default Trigger getTrigger() {
		return new NoneTrigger();
	}

	default String getDescription() {
		return null;
	}

	default int getRetries() {
		return 0;
	}

	default int getWeight() {
		return 100;
	}

	default long getTimeout() {
		return -1L;
	}

	default String getEmail() {
		return null;
	}

	default JobKey[] getDependentKeys() {
		return null;
	}

	default JobKey[] getSubKeys() {
		return null;
	}

	default float getCompletionRate() {
		return -1F;
	}

}
