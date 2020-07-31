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

	void pauseJob(Job job);

	void resumeJob(Job job);

	ResultSetSlice<JobStat> getJobInfos();

}
