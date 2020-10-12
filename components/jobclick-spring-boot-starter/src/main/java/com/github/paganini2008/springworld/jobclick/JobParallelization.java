package com.github.paganini2008.springworld.jobclick;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.github.paganini2008.devtools.ArrayUtils;
import com.github.paganini2008.springworld.jobclick.model.JobDetail;
import com.github.paganini2008.springworld.jobclick.model.JobPeerResult;
import com.github.paganini2008.springworld.jobclick.model.JobTeamResult;
import com.github.paganini2008.springworld.reditools.common.RedisCountDownLatch;
import com.github.paganini2008.springworld.reditools.messager.RedisMessageSender;

/**
 * 
 * JobParallelization
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class JobParallelization implements Job {

	private final Job delegate;
	private final JobKey[] dependencies;
	private final Float completionRate;

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
	private RedisMessageSender redisMessageSender;

	@Autowired
	private JobManager jobManager;

	public JobParallelization(Job delegate, JobKey[] dependencies, Float completionRate) {
		this.delegate = delegate;
		this.dependencies = dependencies;
		this.completionRate = completionRate;
	}

	@Override
	public String getJobName() {
		return delegate.getJobName();
	}

	@Override
	public String getJobClassName() {
		return delegate.getJobClassName();
	}

	@Override
	public String getClusterName() {
		return delegate.getClusterName();
	}

	@Override
	public String getGroupName() {
		return delegate.getGroupName();
	}

	@Override
	public Trigger getTrigger() {
		return delegate.getTrigger();
	}

	@Override
	public String getDescription() {
		return delegate.getDescription();
	}

	@Override
	public int getRetries() {
		return delegate.getRetries();
	}

	@Override
	public int getWeight() {
		return delegate.getWeight();
	}

	@Override
	public long getTimeout() {
		return delegate.getTimeout();
	}

	@Override
	public String getEmail() {
		return delegate.getEmail();
	}

	@Override
	public boolean managedByApplicationContext() {
		return false;
	}

	@Override
	public Object execute(JobKey jobKey, Object attachment, Logger log) throws Exception {
		for (JobKey dependency : dependencies) {
			Job job = getJob(dependency);
			JobDetail jobDetail = jobManager.getJobDetail(jobKey, false);
			jobExecutor.execute(job, jobDetail.getAttachment(), 0);
			log.trace("Run job dependency: " + dependency);
		}
		log.trace("Job '{}' is waiting for all dependent job done ...", jobKey);
		RedisCountDownLatch latch = new RedisCountDownLatch(jobKey.getIdentifier(), redisMessageSender);
		Object[] answer = delegate.getTimeout() > 0 ? latch.await(dependencies.length, delegate.getTimeout(), TimeUnit.MILLISECONDS, null)
				: latch.await(dependencies.length, null);
		if (ArrayUtils.isNotEmpty(answer)) {
			int totalWeight = 0, completionWeight = 0;
			for (Object result : answer) {
				JobPeerResult jobResult = (JobPeerResult) result;
				JobDetail jobDetail = jobManager.getJobDetail(jobResult.getJobKey(), false);
				totalWeight += jobDetail.getWeight();
				completionWeight += jobResult.isApproved() ? jobDetail.getWeight() : 0;
			}
			boolean run = completionRate != null ? (float) completionWeight / totalWeight >= completionRate.floatValue() : true;
			if (run) {
				log.trace("Do run job '{}' after all job peers done.", jobKey);
				JobTeamResult teamResult = new JobTeamResult(jobKey, attachment, ArrayUtils.cast(answer, JobPeerResult.class));
				jobExecutor.execute(delegate, teamResult, 0);
			}
		}
		return null;
	}

	private Job getJob(JobKey jobKey) throws Exception {
		Job job = jobBeanLoader.loadJobBean(jobKey);
		if (job == null && externalJobBeanLoader != null) {
			job = externalJobBeanLoader.loadJobBean(jobKey);
		}
		return job;
	}

}
