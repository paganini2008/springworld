package com.github.paganini2008.springworld.scheduler;

/**
 * 
 * Job
 *
 * @author Fred Feng
 * @since 1.0
 */
public interface Job extends JobDefinition {

	default void prepare() {
	}

	default boolean onSuccess(Object result) {
		return true;
	}

	default boolean onFailure(Throwable e) {
		return true;
	}

	default boolean shouldRun() {
		return true;
	}

	Object execute(Object result);

}
