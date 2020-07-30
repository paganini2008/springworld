package com.github.paganini2008.springworld.scheduler;

import com.github.paganini2008.devtools.jdbc.ResultSetSlice;

/**
 * 
 * JobManager
 * 
 * @author Fred Feng
 * @version 1.0
 */
public interface JobManager extends JobPersistence, Lifecycle {

	void schedule(Job job, Object attachment) throws JobException;

	boolean hasScheduled(Job job) throws JobException;

	void pauseJob(Job job) throws JobException;

	void resumeJob(Job job) throws JobException;

	void unscheduleJob(Job job);

	void runJob(Job job, Object attachment);

	void doSchedule();

	int countOfScheduling();

	Future getFuture(Job job);

	ResultSetSlice<JobStat> getJobInfos();

}
