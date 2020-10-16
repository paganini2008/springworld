package com.github.paganini2008.springworld.jobswarm;

import com.github.paganini2008.springworld.jobswarm.model.JobPersistParam;

/**
 * 
 * JobPersistence
 *
 * @author Fred Feng
 */
public interface JobPersistence {

	default int persistJob(JobPersistParam param) throws Exception {
		throw new UnsupportedOperationException("persistJob");
	}

	default int persistJob(JobDefinition jobDef, String attachment) throws Exception {
		throw new UnsupportedOperationException("persistJob");
	}

	default JobState deleteJob(JobKey jobKey) throws Exception {
		throw new UnsupportedOperationException("deleteJob");
	}

	default boolean hasJob(JobKey jobKey) throws Exception {
		return true;
	}

}
