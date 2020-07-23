package com.github.paganini2008.springworld.scheduler;

import com.github.paganini2008.devtools.jdbc.ResultSetSlice;

/**
 * 
 * JobStore
 *
 * @author Fred Feng
 * @since 1.0
 */
public interface JobStore {

	void initialize() throws Exception;

	void loadExistedJobs(JobLoadingCallback callback) throws Exception;

	void addJob(Job job) throws Exception;

	void saveJobDepentency(SerializableJob job) throws Exception;

	void deleteJob(Job job) throws Exception;

	boolean hasJob(Job job) throws Exception;

	void setJobState(Job job, JobState jobState) throws Exception;

	ResultSetSlice<JobInfo> getJobInfos() throws Exception;

}
