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

	default void addJob(Job job) throws Exception {
	}

	default void deleteJob(Job job) throws Exception {
	}

	default boolean hasJob(Job job) throws Exception {
		return true;
	}

	void pauseJob(Job job) throws Exception;

	void resumeJob(Job job) throws Exception;

	JobDetail getJobDetail(Job job) throws Exception;

	TriggerDetail getTriggerDetail(Job job) throws Exception;

	JobRuntime getJobRuntime(Job job) throws Exception;

	JobStat getJobStat(Job job) throws Exception;

	ResultSetSlice<JobInfo> getJobInfo() throws Exception;

	ResultSetSlice<JobStat> getJobStat(StatType statType) throws Exception;

}
