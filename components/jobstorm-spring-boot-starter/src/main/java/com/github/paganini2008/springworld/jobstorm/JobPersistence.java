package com.github.paganini2008.springworld.jobstorm;

import com.github.paganini2008.springworld.jobstorm.model.JobPersistParam;
import com.github.paganini2008.springworld.jobstorm.utils.GenericJobDefinition;

/**
 * 
 * JobPersistence
 *
 * @author Fred Feng
 */
public interface JobPersistence {

	default int persistJob(JobPersistParam param) throws Exception {
		JobDefinition jobDefinition = GenericJobDefinition.parse(param).build();
		return persistJob(jobDefinition, param.getAttachment());
	}

	default int persistJob(JobDefinition jobDefinition, String attachment) throws Exception {
		throw new UnsupportedOperationException("persistJob");
	}

	default JobState deleteJob(JobKey jobKey) throws Exception {
		throw new UnsupportedOperationException("deleteJob");
	}

	default boolean hasJob(JobKey jobKey) throws Exception {
		return true;
	}

}
