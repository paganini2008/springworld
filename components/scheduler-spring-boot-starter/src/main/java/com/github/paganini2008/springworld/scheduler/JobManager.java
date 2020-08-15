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

	default void deleteJob(JobKey jobKey) throws SQLException {
	}

	default boolean hasJob(JobKey jobKey) throws SQLException {
		return true;
	}

	void pauseJob(JobKey jobKey) throws SQLException;

	void resumeJob(JobKey jobKey) throws SQLException;

	boolean hasJobState(JobKey jobKey, JobState jobState) throws SQLException;

	void setJobState(JobKey jobKey, JobState jobState) throws SQLException;

	JobDetail getJobDetail(JobKey jobKey) throws SQLException;

	JobTriggerDetail getJobTriggerDetail(JobKey jobKey) throws SQLException;

	JobRuntime getJobRuntime(JobKey jobKey) throws SQLException;

	JobStat getJobStat(JobKey jobKey) throws SQLException;

	ResultSetSlice<JobInfo> getJobInfo() throws SQLException;

	ResultSetSlice<JobStat> getJobStat(StatType statType) throws SQLException;

}
