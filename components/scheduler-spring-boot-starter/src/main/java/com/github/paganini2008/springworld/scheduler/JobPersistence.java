package com.github.paganini2008.springworld.scheduler;

/**
 * 
 * JobPersistence
 *
 * @author Fred Feng
 */
public interface JobPersistence {

	default void addJob(Job job) throws JobException {
	}

	default void deleteJob(Job job) throws JobException {
	}

	default boolean hasJob(Job job) throws JobException {
		return true;
	}

}
