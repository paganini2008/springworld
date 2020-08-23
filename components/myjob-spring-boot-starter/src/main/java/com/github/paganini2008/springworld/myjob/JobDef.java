package com.github.paganini2008.springworld.myjob;

/**
 * 
 * JobDef
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public interface JobDef {

	default String getJobName() {
		String simpleName = getClass().getSimpleName();
		return simpleName.substring(0, 1).toLowerCase().concat(simpleName.substring(1));
	}

	default String getJobClassName() {
		return getClass().getName();
	}

	String getGroupName();
	
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
