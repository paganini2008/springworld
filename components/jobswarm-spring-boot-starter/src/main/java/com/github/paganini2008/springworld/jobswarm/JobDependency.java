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

	default float getCompletionRate() {
		return -1F;
	}

	default boolean approve(JobKey jobKey, RunningState runningState, Object attachment, Object result) {
		return runningState == RunningState.COMPLETED;
	}

}
