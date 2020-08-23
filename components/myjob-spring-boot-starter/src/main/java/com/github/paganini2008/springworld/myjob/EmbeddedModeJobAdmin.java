package com.github.paganini2008.springworld.myjob;

import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * 
 * EmbeddedModeJobAdmin
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class EmbeddedModeJobAdmin implements JobAdmin {

	@Qualifier(BeanNames.MAIN_JOB_EXECUTOR)
	@Autowired
	private JobExecutor jobExecutor;

	@Qualifier("internal-job-bean-loader")
	@Autowired
	private JobBeanLoader jobBeanLoader;

	@Qualifier("external-job-bean-loader")
	@Autowired(required = false)
	private JobBeanLoader externalJobBeanLoader;

	@Autowired
	private JobManager jobManager;

	public JobState persistJob(JobConfig jobConfig) {
		JobDef jobDef = JobPersistRequest.build(jobConfig);
		try {
			jobManager.persistJob(jobDef, jobConfig.getAttachment());
			return jobManager.getJobRuntime(JobKey.of(jobDef)).getJobState();
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

	@Override
	public JobState triggerJob(JobKey jobKey, Object attachment) {
		try {
			Job job = jobBeanLoader.loadJobBean(jobKey);
			if (job == null && externalJobBeanLoader != null) {
				job = externalJobBeanLoader.loadJobBean(jobKey);
			}
			jobExecutor.execute(job, attachment);
			return jobManager.getJobRuntime(jobKey).getJobState();
		} catch (Exception e) {
			throw new JobException(e.getMessage(), e);
		}
	}

}
