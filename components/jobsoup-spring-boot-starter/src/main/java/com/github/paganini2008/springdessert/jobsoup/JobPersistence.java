package com.github.paganini2008.springdessert.jobsoup;

import com.github.paganini2008.springdessert.jobsoup.model.JobPersistParam;
import com.github.paganini2008.springdessert.jobsoup.utils.GenericJobDefinition;

/**
 * 
 * JobPersistence
 *
 * @author Jimmy Hoff
 */
public interface JobPersistence {

	default int persistJob(JobPersistParam param) throws Exception {
		JobDefinition jobDefinition = GenericJobDefinition.parse(param).build();
		return persistJob(jobDefinition, param.getAttachment());
	}

	default int persistJob(JobDefinition jobDefinition, String attachment) throws Exception {
		throw new UnsupportedOperationException("persistJob");
	}

	default JobState finishJob(JobKey jobKey) throws Exception {
		throw new UnsupportedOperationException("finishJob");
	}

	default boolean hasJob(JobKey jobKey) throws Exception {
		return true;
	}
	
	JobState pauseJob(JobKey jobKey) throws Exception;

	JobState resumeJob(JobKey jobKey) throws Exception;

	boolean hasJobState(JobKey jobKey, JobState jobState) throws Exception;

	JobState setJobState(JobKey jobKey, JobState jobState) throws Exception;

}
