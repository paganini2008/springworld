package com.github.paganini2008.springworld.jobswarm;

import java.lang.reflect.Method;

import org.springframework.beans.factory.annotation.Autowired;

import com.github.paganini2008.devtools.ClassUtils;
import com.github.paganini2008.devtools.beans.BeanUtils;
import com.github.paganini2008.devtools.proxy.Aspect;
import com.github.paganini2008.devtools.proxy.JdkProxyFactory;
import com.github.paganini2008.devtools.proxy.ProxyFactory;
import com.github.paganini2008.devtools.reflection.MethodUtils;
import com.github.paganini2008.springworld.cluster.utils.ApplicationContextUtils;
import com.github.paganini2008.springworld.jobswarm.model.JobDetail;
import com.github.paganini2008.springworld.jobswarm.model.JobTriggerDetail;
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
				log.trace("Can not load JobClass by name '" + jobClassName + "' into job instance.");
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
			return parallelizeJobIfNecessary(jobKey, job);
		} else if (NotManagedJob.class.isAssignableFrom(jobClass)) {
			NotManagedJob target = ApplicationContextUtils.autowireBean((NotManagedJob) BeanUtils.instantiate(jobClass));
			JobDetail jobDetail = jobManager.getJobDetail(jobKey, false);
			Job job = (Job) proxyFactory.getProxy(target, new JobBeanAspect(jobDetail), Job.class);
			return parallelizeJobIfNecessary(jobKey, job);
		}
		throw new JobException("Class '" + jobClass.getName() + "' is not a instance of interface '" + Job.class.getName() + "'.");
	}

	private Job parallelizeJobIfNecessary(JobKey jobKey, Job job) throws Exception {
		JobTriggerDetail triggerDetail = jobManager.getJobTriggerDetail(jobKey);
		if (triggerDetail.getTriggerType() != TriggerType.DEPENDENT) {
			return job;
		}
		Dependency dependency = triggerDetail.getTriggerDescriptionObject().getDependency();
		DependencyType dependencyType = dependency.getDependencyType();
		if (dependencyType != DependencyType.PARALLEL) {
			return job;
		}
		return ApplicationContextUtils
				.autowireBean(new JobParallelization(job, dependency.getDependencies(), dependency.getCompletionRate()));
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

		JobBeanAspect(JobDetail jobDetail) {
			this.jobDetail = jobDetail;
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
			case "getTrigger":
				return null;
			default:
				return MethodUtils.invokeMethod(target, method, args);
			}
		}

	}
}
