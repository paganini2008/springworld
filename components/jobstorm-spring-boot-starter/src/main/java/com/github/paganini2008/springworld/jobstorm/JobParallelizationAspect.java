package com.github.paganini2008.springworld.jobstorm;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.github.paganini2008.devtools.ArrayUtils;
import com.github.paganini2008.devtools.NumberUtils;
import com.github.paganini2008.devtools.date.Duration;
import com.github.paganini2008.devtools.proxy.Aspect;
import com.github.paganini2008.devtools.reflection.MethodUtils;
import com.github.paganini2008.springworld.cluster.utils.ApplicationContextUtils;
import com.github.paganini2008.springworld.jobstorm.model.JobDetail;
import com.github.paganini2008.springworld.jobstorm.model.JobParallelizingResult;
import com.github.paganini2008.springworld.jobstorm.model.JobPeerResult;
import com.github.paganini2008.springworld.reditools.common.RedisCountDownLatch;
import com.github.paganini2008.springworld.reditools.messager.RedisMessageSender;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * JobParallelizationAspect
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Slf4j
public class JobParallelizationAspect implements Aspect {

	private final JobKey[] dependencies;
	private final float completionRate;

	JobParallelizationAspect(JobKey[] dependencies, float completionRate) {
		this.dependencies = dependencies;
		this.completionRate = completionRate;
	}

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

	@Override
	public Object call(Object target, Method method, Object[] args) throws Throwable {
		if (method.getName().equals("execute")) {
			final Job targetJob = (Job) target;
			final JobKey jobKey = (JobKey) args[0];
			final Object attachment = args[1];
			final Logger logger = (Logger) args[2];

			log.trace("Start to parallel all dependent jobs ...");
			long startTime = System.currentTimeMillis();
			long maxTimeout = 0;
			for (JobKey dependency : dependencies) {
				log.trace("Parallel run dependent job: " + dependency);
				Job job = getJob(dependency);
				JobDetail jobDetail = jobManager.getJobDetail(dependency, false);
				jobExecutor.execute(job, jobDetail.getAttachment(), 0);
				maxTimeout = Long.max(maxTimeout, job.getTimeout());
			}
			RedisCountDownLatch latch = new RedisCountDownLatch(jobKey.getIdentifier(), redisMessageSender);
			Object[] answers = maxTimeout > 0 ? latch.await(dependencies.length, maxTimeout, TimeUnit.MILLISECONDS, null)
					: latch.await(dependencies.length, null);
			if (ArrayUtils.isNotEmpty(answers)) {
				log.trace("Parallellizing job '{}' completedly", jobKey);
				if (answers.length == dependencies.length) {
					log.trace("All dependent jobs run ok. Take time: " + Duration.HOUR.format(System.currentTimeMillis() - startTime));
				} else {
					log.warn("Maybe some dependent job spend too much time. Please check them.");
				}
				int totalWeight = 0, completionWeight = 0;
				for (Object result : answers) {
					JobPeerResult jobResult = (JobPeerResult) result;
					JobDetail jobDetail = jobManager.getJobDetail(jobResult.getJobKey(), false);
					totalWeight += jobDetail.getWeight();
					completionWeight += approveIfJobCompleted(targetJob, jobResult) ? jobDetail.getWeight() : 0;
				}
				float rate = (float) completionWeight / totalWeight;
				if (rate >= completionRate) {
					log.trace("The completionRate is '{}' and now start to run job '{}'.", NumberUtils.format(rate, 2), jobKey);
					JobParallelizingResult parallelizingResult = new JobParallelizingResult(jobKey, attachment,
							ArrayUtils.cast(answers, JobPeerResult.class));
					return targetJob.execute(jobKey, parallelizingResult, logger);
				}
			}
			return null;
		}
		return MethodUtils.invokeMethod(target, method, args);
	}

	private boolean approveIfJobCompleted(Job targetJob, JobPeerResult jobResult) {
		if (ArrayUtils.isNotEmpty(targetJob.getDependencyHandlers())) {
			boolean result = true;
			for (Class<?> handlerClass : targetJob.getDependencyHandlers()) {
				DependencyHandler handler = (DependencyHandler) ApplicationContextUtils.getBeanIfAbsent(handlerClass);
				result &= handler.approve(jobResult.getJobKey(), jobResult.getRunningState(), jobResult.getAttachment(),
						jobResult.getResult());
			}
			return result;
		} else {
			return jobResult.getRunningState() == RunningState.COMPLETED;
		}
	}

	private Job getJob(JobKey jobKey) throws Exception {
		Job job = jobBeanLoader.loadJobBean(jobKey);
		if (job == null && externalJobBeanLoader != null) {
			job = externalJobBeanLoader.loadJobBean(jobKey);
		}
		return job;
	}

}
