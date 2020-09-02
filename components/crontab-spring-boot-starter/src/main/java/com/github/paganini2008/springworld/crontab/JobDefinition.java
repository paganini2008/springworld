package com.github.paganini2008.springworld.crontab;

/**
 * 
 * JobDefinition
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public interface JobDefinition {

	default String getClusterName() {
		return null;
	}
	
	default String getGroupName() {
		return null;
	}
	
	default String getJobName() {
		String simpleName = getClass().getSimpleName();
		return simpleName.substring(0, 1).toLowerCase().concat(simpleName.substring(1));
	}

	default String getJobClassName() {
		return getClass().getName();
	}
	
	TriggerBuilder buildTrigger();

	default String getDescription() {
		return "";
	}

	default int getRetries() {
		return 0;
	}

	default String getEmail() {
		return null;
	}
}
