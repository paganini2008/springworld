package com.github.paganini2008.springworld.myjob;

/**
 * 
 * Job
 *
 * @author Fred Feng
 * @since 1.0
 */
public interface Job extends NotManagedJob, JobDef {

	default boolean managedByApplicationContext() {
		return true;
	}

}
