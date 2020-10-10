package com.github.paganini2008.springworld.jobclick;

import com.github.paganini2008.springworld.jobclick.model.JobPersistParam;

/**
 * 
 * JobPersistence
 *
 * @author Fred Feng
 */
public interface JobPersistence {

	default int persistJob(JobPersistParam param) throws Exception {
		JobDefinition jobDefinition = JobPersistRequest.build(param);
		return persistJob(jobDefinition, param.getAttachment());
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
