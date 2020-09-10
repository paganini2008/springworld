package com.github.paganini2008.springworld.crontab.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.github.paganini2008.springworld.crontab.BeanNames;
import com.github.paganini2008.springworld.crontab.Job;
import com.github.paganini2008.springworld.crontab.JobAdmin;
import com.github.paganini2008.springworld.crontab.JobBeanLoader;
import com.github.paganini2008.springworld.crontab.JobExecutor;
import com.github.paganini2008.springworld.crontab.JobKey;
import com.github.paganini2008.springworld.crontab.JobManager;
import com.github.paganini2008.springworld.crontab.JobState;
import com.github.paganini2008.springworld.crontab.model.JobPersistParam;

/**
 * 
 * ConsumerModeJobAdmin
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class ConsumerModeJobAdmin implements JobAdmin {

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

	@Override
	public JobState persistJob(JobPersistParam jobConfig) {
		throw new UnsupportedOperationException("persistJob");
	}

	@Override
	public JobState deleteJob(JobKey jobKey) {
		throw new UnsupportedOperationException("deleteJob");
	}

	@Override
	public JobState hasJob(JobKey jobKey) {
		throw new UnsupportedOperationException("hasJob");
	}

	@Override
	public JobState triggerJob(JobKey jobKey, Object attachment) throws Exception {
		Job job = loadJobBean(jobKey);
		jobExecutor.execute(job, attachment, 0);
		return jobManager.getJobRuntime(jobKey).getJobState();
	}

	private Job loadJobBean(JobKey jobKey) throws Exception {
		Job job = jobBeanLoader.loadJobBean(jobKey);
		if (job == null && externalJobBeanLoader != null) {
			job = externalJobBeanLoader.loadJobBean(jobKey);
		}
		return job;
	}

}
