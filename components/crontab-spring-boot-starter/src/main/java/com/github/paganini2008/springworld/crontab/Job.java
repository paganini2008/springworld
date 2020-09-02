package com.github.paganini2008.springworld.crontab;

/**
 * 
 * Job
 *
 * @author Fred Feng
 * @since 1.0
 */
public interface Job extends NotManagedJob, JobDefinition {

	default boolean managedByApplicationContext() {
		return true;
	}

}
