package com.github.paganini2008.springworld.jobswarm;

/**
 * 
 * JobDependency
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public interface JobDependency {

	default DependencyType getDependencyType() {
		return DependencyType.SERIAL;
	}

	JobKey[] getDependencies();

	default Float getCompletionRate() {
		return null;
	}

	default boolean approve(JobKey jobKey, RunningState runningState, Object attachment, Object result) {
		return runningState == RunningState.COMPLETED;
	}

}
