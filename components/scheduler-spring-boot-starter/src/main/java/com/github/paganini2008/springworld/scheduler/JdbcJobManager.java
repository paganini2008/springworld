package com.github.paganini2008.springworld.scheduler;

import org.springframework.beans.factory.annotation.Autowired;

import com.github.paganini2008.devtools.jdbc.ResultSetSlice;

/**
 * 
 * JdbcJobManager
 *
 * @author Fred Feng
 * @since 1.0
 */
public class JdbcJobManager extends AbstractJobManager implements JobManager {

	@Autowired
	private JobStore jobStore;

	@Override
	public void addJob(Job job) {
		try {
			jobStore.addJob(job);
		} catch (Exception e) {
			throw new JobException(e.getMessage(), e);
		}
	}

	@Override
	public void deleteJob(Job job) {
		try {
			jobStore.deleteJob(job);
		} catch (Exception e) {
			throw new JobException(e.getMessage(), e);
		}
	}

	@Override
	public boolean hasJob(Job job) {
		try {
			return jobStore.hasJob(job);
		} catch (Exception e) {
			throw new JobException(e.getMessage(), e);
		}
	}

	@Override
	protected void setJobState(Job job, JobState jobState) {
		try {
			jobStore.setJobState(job, jobState);
		} catch (Exception e) {
			throw new JobException(e.getMessage(), e);
		}
	}
	
	@Override
	public ResultSetSlice<JobInfo> getJobInfos() {
		return null;
	}

}
