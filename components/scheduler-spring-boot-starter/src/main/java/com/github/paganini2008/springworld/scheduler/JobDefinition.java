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

	String getJobName();

	String getJobClassName();

	default String getSignature() {
		return getJobName() + "@" + getJobClassName();
	}

	String getGroupName();

	String getDescription();

	int getRetries();

	String getAttachment();
}
