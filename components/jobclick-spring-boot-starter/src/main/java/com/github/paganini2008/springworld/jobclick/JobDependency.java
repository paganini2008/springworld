package com.github.paganini2008.springworld.jobclick;

/**
 * 
 * JobDependency
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public interface JobDependency {

	JobKey[] getDependencies();

	default Float getCompletionRate() {
		return null;
	}

	default DependencyType getDependencyType() {
		return DependencyType.NONE;
	}

}
