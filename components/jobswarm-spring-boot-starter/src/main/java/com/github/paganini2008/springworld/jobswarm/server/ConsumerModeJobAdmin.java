package com.github.paganini2008.springworld.jobswarm.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

import com.github.paganini2008.springworld.cluster.multicast.ClusterMulticastGroup;
import com.github.paganini2008.springworld.jobswarm.BeanNames;
import com.github.paganini2008.springworld.jobswarm.Job;
import com.github.paganini2008.springworld.jobswarm.JobAdmin;
import com.github.paganini2008.springworld.jobswarm.JobBeanLoader;
import com.github.paganini2008.springworld.jobswarm.JobExecutor;
import com.github.paganini2008.springworld.jobswarm.JobKey;
import com.github.paganini2008.springworld.jobswarm.JobLifeCycle;
import com.github.paganini2008.springworld.jobswarm.JobManager;
import com.github.paganini2008.springworld.jobswarm.JobState;
import com.github.paganini2008.springworld.jobswarm.LifeCycleListenerContainer;
import com.github.paganini2008.springworld.jobswarm.model.JobLifeCycleParam;

/**
 * 
 * ConsumerModeJobAdmin
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class ConsumerModeJobAdmin implements JobAdmin {

	@Value("${spring.application.name}")
	private String applicationName;

	@Qualifier(BeanNames.MAIN_JOB_EXECUTOR)
	@Autowired
	private JobExecutor jobExecutor;

	@Qualifier("jobParalelizationJobExecutor")
	@Autowired
	private JobExecutor jobParalelizationJobExecutor;

	@Qualifier(BeanNames.INTERNAL_JOB_BEAN_LOADER)
	@Autowired
	private JobBeanLoader jobBeanLoader;

	@Qualifier(BeanNames.EXTERNAL_JOB_BEAN_LOADER)
	@Autowired(required = false)
	private JobBeanLoader externalJobBeanLoader;

	@Autowired
	private JobManager jobManager;

	@Autowired
	private ClusterMulticastGroup clusterMulticastGroup;

	@Override
	public JobState triggerJob(JobKey jobKey, Object attachment) throws Exception {
		Job job = loadJobBean(jobKey);
		jobExecutor.execute(job, attachment, 0);
		return jobManager.getJobRuntime(jobKey).getJobState();
	}

	@Override
	public void publicLifeCycleEvent(JobKey jobKey, JobLifeCycle lifeCycle) {
		clusterMulticastGroup.multicast(applicationName, LifeCycleListenerContainer.class.getName(),
				new JobLifeCycleParam(jobKey, lifeCycle));
	}

	private Job loadJobBean(JobKey jobKey) throws Exception {
		Job job = jobBeanLoader.loadJobBean(jobKey);
		if (job == null && externalJobBeanLoader != null) {
			job = externalJobBeanLoader.loadJobBean(jobKey);
		}
		return job;
	}

}
