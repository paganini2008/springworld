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
	public void configure() throws Exception {
		jobStore.reloadJobs((job, attachment) -> {
			schedule(job, attachment);
		});
	}

	@Override
	public void addJob(Job job) throws JobException {
		try {
			jobStore.addJob(job);
		} catch (Exception e) {
			throw new JobException(e.getMessage(), e);
		}
	}

	@Override
	public void deleteJob(Job job) throws JobException {
		try {
			jobStore.deleteJob(job);
		} catch (Exception e) {
			throw new JobException(e.getMessage(), e);
		}
	}

	@Override
	public boolean hasJob(Job job) throws JobException {
		try {
			return jobStore.hasJob(job);
		} catch (Exception e) {
			throw new JobException(e.getMessage(), e);
		}
	}

	@Override
	protected void setJobState(Job job, JobState jobState) throws JobException {
		try {
			jobStore.setJobState(job, jobState);
		} catch (Exception e) {
			throw new JobException(e.getMessage(), e);
		}
	}

	@Override
	public ResultSetSlice<JobStat> getJobInfos() {
		return null;
	}

}
