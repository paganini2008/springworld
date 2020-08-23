package com.github.paganini2008.springworld.myjob;

import com.github.paganini2008.devtools.NotImplementedException;

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

	default String getGroupName() {
		throw new NotImplementedException("Please define a groupName for the job.");
	}

	default TriggerBuilder buildTrigger() {
		throw new NotImplementedException("Please define a triggerBuilder for the job.");
	}

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
