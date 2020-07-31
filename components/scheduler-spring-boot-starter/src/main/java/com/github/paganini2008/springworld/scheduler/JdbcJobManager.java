package com.github.paganini2008.springworld.scheduler;

import org.springframework.beans.factory.annotation.Autowired;

import com.github.paganini2008.devtools.jdbc.ResultSetSlice;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * JdbcJobManager
 *
 * @author Fred Feng
 * @since 1.0
 */
@Slf4j
public class JdbcJobManager implements JobManager {

	@Autowired
	private JobStore jobStore;

	@Autowired
	private ScheduleManager scheduleManager;

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
	public void pauseJob(Job job) throws JobException {
		if (scheduleManager.hasScheduled(job)) {
			try {
				jobStore.setJobState(job, JobState.PAUSED);
				if (log.isTraceEnabled()) {
					log.trace("Pause the job: " + job.getSignature());
				}
			} catch (Exception e) {
				throw new JobException(e.getMessage(), e);
			}
		}
	}

	@Override
	public void resumeJob(Job job) throws JobException {
		if (scheduleManager.hasScheduled(job)) {
			try {
				jobStore.setJobState(job, JobState.RUNNING);
				if (log.isTraceEnabled()) {
					log.trace("Pause the job: " + job.getSignature());
				}
			} catch (Exception e) {
				throw new JobException(e.getMessage(), e);
			}
		}
	}

	@Override
	public ResultSetSlice<JobStat> getJobInfos() {
		return null;
	}

}
