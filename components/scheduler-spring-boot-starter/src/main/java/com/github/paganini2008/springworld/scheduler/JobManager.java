package com.github.paganini2008.springworld.scheduler;

import com.github.paganini2008.devtools.jdbc.ResultSetSlice;

/**
 * 
 * JobManager
 *
 * @author Fred Feng
 * @since 1.0
 */
public interface JobManager extends Lifecycle {

	default int addJob(Job job, String attachment) throws Exception {
		return 0;
	}

	default void deleteJob(Job job) throws Exception {
	}

	default boolean hasJob(Job job) throws Exception {
		return true;
	}

	void pauseJob(Job job) throws Exception;

	void resumeJob(Job job) throws Exception;

	boolean hasJobState(Job job, JobState jobState) throws Exception;

	void setJobState(Job job, JobState jobState) throws Exception;

	JobDetail getJobDetail(Job job) throws Exception;

	JobTrigger getJobTrigger(Job job) throws Exception;

	JobRuntime getJobRuntime(Job job) throws Exception;

	JobStat getJobStat(Job job) throws Exception;

	ResultSetSlice<JobInfo> getJobInfo() throws Exception;

	ResultSetSlice<JobStat> getJobStat(StatType statType) throws Exception;

}
