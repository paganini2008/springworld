package com.github.paganini2008.springworld.scheduler;

import java.sql.SQLException;

import com.github.paganini2008.devtools.jdbc.ResultSetSlice;

/**
 * 
 * JobManager
 *
 * @author Fred Feng
 * @since 1.0
 */
public interface JobManager extends Lifecycle {

	default int addJob(Job job, String attachment) throws SQLException {
		return 0;
	}

	default void deleteJob(Job job) throws SQLException {
	}

	default boolean hasJob(Job job) throws SQLException {
		return true;
	}

	void pauseJob(Job job) throws SQLException;

	void resumeJob(Job job) throws SQLException;

	boolean hasJobState(Job job, JobState jobState) throws SQLException;

	void setJobState(Job job, JobState jobState) throws SQLException;

	JobDetail getJobDetail(Job job) throws SQLException;

	JobTrigger getJobTrigger(Job job) throws SQLException;

	JobRuntime getJobRuntime(Job job) throws SQLException;

	JobStat getJobStat(Job job) throws SQLException;

	ResultSetSlice<JobInfo> getJobInfo() throws SQLException;

	ResultSetSlice<JobStat> getJobStat(StatType statType) throws SQLException;

}
