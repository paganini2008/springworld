package com.github.paganini2008.springworld.scheduler;

import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * EmbeddedModeJobAdmin
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class EmbeddedModeJobAdmin implements JobAdmin {

	@Autowired
	private JobManager jobManager;

	public JobState persistJob(JobConfig jobConfig) {
		Job job = JobPersistRequest.build(jobConfig);
		try {
			jobManager.persistJob(job, jobConfig.getAttachment());
			return jobManager.getJobRuntime(JobKey.of(job)).getJobState();
		} catch (SQLException e) {
			throw new JobException(e.getMessage(), e);
		}

	}

	public JobState deleteJob(JobKey jobKey) {
		try {
			jobManager.deleteJob(jobKey);
			return jobManager.getJobRuntime(jobKey).getJobState();
		} catch (SQLException e) {
			throw new JobException(e.getMessage(), e);
		}
	}

	public JobState hasJob(JobKey jobKey) {
		try {
			if (jobManager.hasJob(jobKey)) {
				return jobManager.getJobRuntime(jobKey).getJobState();
			} else {
				return JobState.NONE;
			}
		} catch (SQLException e) {
			throw new JobException(e.getMessage(), e);
		}
	}

}
