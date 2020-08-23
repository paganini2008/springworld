package com.github.paganini2008.springworld.myjob;

/**
 * 
 * JobPersistence
 *
 * @author Fred Feng
 */
public interface JobPersistence {

	default void addJob(Job job) {
	}

	default void deleteJob(Job job) {
	}

	default boolean hasJob(Job job) {
		return true;
	}

}
