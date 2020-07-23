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

	void schedule(Job job, Object arg);

	boolean hasScheduled(Job job);

	void pauseJob(Job job) throws Exception;

	void resumeJob(Job job) throws Exception;

	void unscheduleJob(Job job);

	void runJob(Job job, Object arg);

	void doSchedule();

	int countOfScheduling();

	Future getFuture(Job job);

	void addJobDependency(SerializableJob job);

	ResultSetSlice<JobInfo> getJobInfos();

}
