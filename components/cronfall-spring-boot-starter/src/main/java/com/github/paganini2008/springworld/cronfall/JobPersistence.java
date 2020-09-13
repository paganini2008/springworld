package com.github.paganini2008.springworld.cronfall;

/**
 * 
 * JobPersistence
 *
 * @author Fred Feng
 */
public interface JobPersistence {

	default int persistJob(JobDefinition jobDef, String attachment) throws Exception {
		return 0;
	}

	default JobState deleteJob(JobKey jobKey) throws Exception {
		return JobState.FINISHED;
	}

	default boolean hasJob(JobKey jobKey) throws Exception {
		return true;
	}

}
