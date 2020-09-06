package com.github.paganini2008.springworld.crontab;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.github.paganini2008.springworld.crontab.model.JobPersistParam;

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

	@Qualifier(BeanNames.INTERNAL_JOB_BEAN_LOADER)
	@Autowired
	private JobBeanLoader jobBeanLoader;

	@Qualifier(BeanNames.EXTERNAL_JOB_BEAN_LOADER)
	@Autowired(required = false)
	private JobBeanLoader externalJobBeanLoader;

	@Autowired
	private JobManager jobManager;

	public JobState persistJob(JobPersistParam jobConfig) throws Exception {
		JobDefinition jobDef = JobPersistRequest.build(jobConfig);
		jobManager.persistJob(jobDef, jobConfig.getAttachment());
		return jobManager.getJobRuntime(JobKey.of(jobDef)).getJobState();
	}

	public JobState deleteJob(JobKey jobKey) throws Exception {
		return jobManager.deleteJob(jobKey);
	}

	public JobState hasJob(JobKey jobKey) throws Exception {
		if (jobManager.hasJob(jobKey)) {
			return jobManager.getJobRuntime(jobKey).getJobState();
		} else {
			return JobState.NONE;
		}
	}

	@Override
	public JobState triggerJob(JobKey jobKey, Object attachment) throws Exception {
		Job job = jobBeanLoader.loadJobBean(jobKey);
		if (job == null && externalJobBeanLoader != null) {
			job = externalJobBeanLoader.loadJobBean(jobKey);
		}
		jobExecutor.execute(job, attachment);
		return jobManager.getJobRuntime(jobKey).getJobState();
	}

}
