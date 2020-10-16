package com.github.paganini2008.springworld.jobswarm;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.github.paganini2008.devtools.ArrayUtils;
import com.github.paganini2008.devtools.NumberUtils;
import com.github.paganini2008.springworld.jobswarm.model.JobDetail;
import com.github.paganini2008.springworld.jobswarm.model.JobParallelizingResult;
import com.github.paganini2008.springworld.jobswarm.model.JobPeerResult;
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
			log.trace("Start to run dependent job: " + dependency);
			Job job = getJob(dependency);
			JobDetail jobDetail = jobManager.getJobDetail(jobKey, false);
			jobExecutor.execute(job, jobDetail.getAttachment(), 0);
		}
		log.trace("Job '{}' is waiting for all dependent jobs done ...", jobKey);
		RedisCountDownLatch latch = new RedisCountDownLatch(jobKey.getIdentifier(), redisMessageSender);
		Object[] answer = delegate.getTimeout() > 0 ? latch.await(dependencies.length, delegate.getTimeout(), TimeUnit.MILLISECONDS, null)
				: latch.await(dependencies.length, null);
		if (ArrayUtils.isNotEmpty(answer)) {
			if (answer.length == dependencies.length) {
				log.trace("All dependent jobs run ok.");
			} else {
				log.warn("Maybe some dependent job spend too much time.");
			}
			int totalWeight = 0, completionWeight = 0;
			for (Object result : answer) {
				JobPeerResult jobResult = (JobPeerResult) result;
				JobDetail jobDetail = jobManager.getJobDetail(jobResult.getJobKey(), false);
				totalWeight += jobDetail.getWeight();
				completionWeight += ((JobDependency) delegate).approve(jobResult.getJobKey(), jobResult.getRunningState(),
						jobResult.getAttachment(), jobResult.getResult()) ? jobDetail.getWeight() : 0;
			}
			boolean run = completionRate != null ? (float) completionWeight / totalWeight >= completionRate.floatValue() : true;
			if (run) {
				log.trace("The completionRate is '{}' and now start to run job '{}' after all dependent jobs done.",
						NumberUtils.format(completionRate, 2), jobKey);
				JobParallelizingResult parallelizingResult = new JobParallelizingResult(jobKey, attachment,
						ArrayUtils.cast(answer, JobPeerResult.class));
				jobExecutor.execute(delegate, parallelizingResult, 0);
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
