package com.github.paganini2008.springworld.joblink;

/**
 * 
 * JobParallelization
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public interface JobParallelization {

	default boolean isParallel() {
		return false;
	}

	default ParallelPolicy getParallelPolicy() {
		return new DefaultParallelPolicy();
	}

}
