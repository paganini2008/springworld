package com.github.paganini2008.springworld.jobswarm;

import java.lang.reflect.Method;

import org.springframework.beans.factory.annotation.Autowired;

import com.github.paganini2008.devtools.Assert;
import com.github.paganini2008.devtools.ClassUtils;
import com.github.paganini2008.devtools.proxy.Aspect;
import com.github.paganini2008.devtools.proxy.JdkProxyFactory;
import com.github.paganini2008.devtools.proxy.ProxyFactory;
import com.github.paganini2008.devtools.reflection.MethodUtils;
import com.github.paganini2008.springworld.cluster.utils.ApplicationContextUtils;
import com.github.paganini2008.springworld.jobswarm.model.JobDetail;
import com.github.paganini2008.springworld.jobswarm.model.JobRuntime;
import com.github.paganini2008.springworld.jobswarm.model.TriggerDescription.Dependency;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * InternalJobBeanLoader
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Slf4j
public class InternalJobBeanLoader implements JobBeanLoader {

	private static final ProxyFactory proxyFactory = new JdkProxyFactory();

	@Autowired
	private JobManager jobManager;

	@Override
	public Job loadJobBean(JobKey jobKey) throws Exception {
		final String jobClassName = jobKey.getJobClassName();
		Class<?> jobClass;
		try {
			jobClass = ClassUtils.forName(jobClassName);
		} catch (RuntimeException e) {
			if (log.isTraceEnabled()) {
				log.trace("Can not load JobClass of name '" + jobClassName + "' to create job instance.");
			}
			return null;
		}
		if (Job.class.isAssignableFrom(jobClass)) {
			final String jobName = jobKey.getJobName();
			Job job = (Job) ApplicationContextUtils.getBean(jobName, jobClass);
			if (job == null) {
				job = (Job) ApplicationContextUtils.getBean(jobClass, bean -> {
					return ((Job) bean).getJobName().equals(jobName);
				});
			}
			if (job == null) {
				throw new JobBeanNotFoundException(jobKey);
			}
			JobDetail jobDetail = jobManager.getJobDetail(jobKey, true);
			if (jobDetail.getJobTriggerDetail().getTriggerType() != TriggerType.DEPENDENT) {
				return job;
			}
			return parallelizeJobIfNecessary(jobKey, job, jobDetail);
			
		} else if (NotManagedJob.class.isAssignableFrom(jobClass)) {
			final NotManagedJob target = (NotManagedJob) ApplicationContextUtils.instantiateClass(jobClass);
			JobDetail jobDetail = jobManager.getJobDetail(jobKey, true);
			Class<?>[] interfaceClasses;
			Dependency dependency;
			if (jobDetail.getJobTriggerDetail().getTriggerType() == TriggerType.DEPENDENT) {
				interfaceClasses = new Class<?>[] { Job.class, JobDependency.class };
				dependency = jobDetail.getJobTriggerDetail().getTriggerDescriptionObject().getDependency();
			} else {
				interfaceClasses = new Class<?>[] { Job.class };
				dependency = null;
			}
			Job job = (Job) proxyFactory.getProxy(target, new JobBeanAspect(jobDetail, dependency), interfaceClasses);
			if (jobDetail.getJobTriggerDetail().getTriggerType() != TriggerType.DEPENDENT) {
				return job;
			}
			return parallelizeJobIfNecessary(jobKey, job, jobDetail);
		}
		throw new JobException("Class '" + jobClass.getName() + "' is not a instance of interface '" + Job.class.getName() + "' or '"
				+ NotManagedJob.class.getName() + "'.");
	}

	private Job parallelizeJobIfNecessary(JobKey jobKey, Job job, JobDetail jobDetail) throws Exception {
		Dependency dependency = jobDetail.getJobTriggerDetail().getTriggerDescriptionObject().getDependency();
		DependencyType dependencyType = dependency.getDependencyType();
		if (dependencyType == DependencyType.PARALLEL) {
			JobRuntime jobRuntime = jobDetail.getJobRuntime();
			if (jobRuntime.getJobState() == JobState.PARALLELIZING) {
				return ApplicationContextUtils.instantiateClass(JobParallelization.class, job, dependency.getDependencies(),
						dependency.getCompletionRate());
			}
		}
		return job;
	}

	/**
	 * 
	 * JobBeanAspect
	 * 
	 * @author Fred Feng
	 *
	 * @since 1.0
	 */
	private static class JobBeanAspect implements Aspect {

		private final JobDetail jobDetail;
		private final Dependency dependency;

		JobBeanAspect(JobDetail jobDetail, Dependency dependency) {
			this.jobDetail = jobDetail;
			this.dependency = dependency;
		}

		@Override
		public Object call(Object target, Method method, Object[] args) {
			final String methodName = method.getName();
			switch (methodName) {
			case "getJobName":
				return jobDetail.getJobKey().getJobName();
			case "getJobClassName":
				return jobDetail.getJobKey().getJobClassName();
			case "getGroupName":
				return jobDetail.getJobKey().getGroupName();
			case "getClusterName":
				return jobDetail.getJobKey().getClusterName();
			case "getDescription":
				return jobDetail.getDescription();
			case "getEmail":
				return jobDetail.getEmail();
			case "getRetries":
				return jobDetail.getRetries();
			case "getTimeout":
				return jobDetail.getTimeout();
			case "getWeight":
				return jobDetail.getWeight();
			case "getTrigger":
				return null;
			case "getDependencyType":
				Assert.isNull(dependency, "Dependency must be required");
				return dependency.getDependencyType();
			case "getDependencies":
				Assert.isNull(dependency, "Dependency must be required");
				return dependency.getDependencies();
			case "getCompletionRate":
				Assert.isNull(dependency, "Dependency must be required");
				return dependency.getCompletionRate();
			default:
				return MethodUtils.invokeMethod(target, method, args);
			}
		}

	}
}
