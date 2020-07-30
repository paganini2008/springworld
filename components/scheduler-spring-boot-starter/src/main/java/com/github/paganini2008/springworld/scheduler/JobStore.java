package com.github.paganini2008.springworld.scheduler;

import com.github.paganini2008.devtools.jdbc.ResultSetSlice;

/**
 * 
 * JobStore
 *
 * @author Fred Feng
 * @since 1.0
 */
public interface JobStore extends Lifecycle {

	void reloadJobs(JobLoadingCallback callback) throws Exception;

	void addJob(Job job) throws Exception;

	void deleteJob(Job job) throws Exception;

	boolean hasJob(Job job) throws Exception;

	void setJobState(Job job, JobState jobState) throws Exception;

	JobDetail getJobDetail(Job job) throws Exception;

	JobRuntime getJobRuntime(Job job) throws Exception;

	JobStat getJobStat(Job job) throws Exception;

	ResultSetSlice<JobInfo> getJobInfo() throws Exception;

	ResultSetSlice<JobStat> getJobStat(StatType statType) throws Exception;

}
