package com.github.paganini2008.springworld.jobswarm;

import java.lang.reflect.Method;
import java.util.Date;

import com.github.paganini2008.devtools.ClassUtils;
import com.github.paganini2008.devtools.proxy.Aspect;
import com.github.paganini2008.devtools.proxy.JdkProxyFactory;
import com.github.paganini2008.devtools.proxy.ProxyFactory;
import com.github.paganini2008.devtools.reflection.MethodUtils;
import com.github.paganini2008.springworld.cluster.utils.ApplicationContextUtils;
import com.github.paganini2008.springworld.jobswarm.model.TriggerDescription;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 
 * JobBuilder
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Accessors(chain = true)
@Setter
@Getter
public final class JobBuilder {

	private final JobKey jobKey;
	private final Class<? extends NotManagedJob> jobClass;

	@SuppressWarnings("unchecked")
	JobBuilder(JobKey jobKey) {
		this(jobKey.getClusterName(), jobKey.getGroupName(), jobKey.getJobName(),
				(Class<? extends NotManagedJob>) ClassUtils.forName(jobKey.getJobClassName()));
	}

	JobBuilder(String clusterName, String groupName, String jobName, Class<? extends NotManagedJob> jobClass) {
		this.jobKey = JobKey.by(clusterName, groupName, jobName, jobClass.getName());
		this.jobClass = jobClass;
	}

	private String description;
	private String email;
	private int retries;
	private int weight = 100;
	private long timeout = -1L;
	private String attachment;

	private TriggerBuilder triggerBuilder;

	public Job build() {
		NotManagedJob target = (NotManagedJob) ApplicationContextUtils.instantiateClass(jobClass);
		Class<?>[] interfaceClasses;
		if (target instanceof JobDependency) {
			interfaceClasses = new Class<?>[] { Job.class, JobDependency.class };
		} else {
			interfaceClasses = new Class<?>[] { Job.class };
		}
		return (Job) proxyFactory.getProxy(target, new JobBeanAspect(this), interfaceClasses);
	}

	private static final ProxyFactory proxyFactory = new JdkProxyFactory();

	private static class JobBeanAspect implements Aspect {

		private final JobBuilder jobBuidler;

		JobBeanAspect(JobBuilder jobBuidler) {
			this.jobBuidler = jobBuidler;
		}

		@Override
		public Object call(Object target, Method method, Object[] args) {
			final String methodName = method.getName();
			switch (methodName) {
			case "getJobName":
				return jobBuidler.getJobKey().getJobName();
			case "getJobClassName":
				return jobBuidler.getJobKey().getJobClassName();
			case "getGroupName":
				return jobBuidler.getJobKey().getGroupName();
			case "getClusterName":
				return jobBuidler.getJobKey().getClusterName();
			case "getDescription":
				return jobBuidler.getDescription();
			case "getEmail":
				return jobBuidler.getEmail();
			case "getRetries":
				return jobBuidler.getRetries();
			case "getTimeout":
				return jobBuidler.getTimeout();
			case "getWeight":
				return jobBuidler.getWeight();
			case "getTrigger":
				return jobBuidler.getTriggerBuilder().build();
			default:
				return MethodUtils.invokeMethod(target, method, args);
			}
		}

	}

	@Accessors(chain = true)
	@Setter
	@Getter
	public static class TriggerBuilder {

		private final TriggerDescription triggerDescription;
		private final TriggerType triggerType;
		private Date startDate;
		private Date endDate;
		private int repeatCount = -1;

		TriggerBuilder() {
			this.triggerDescription = new TriggerDescription();
			this.triggerType = TriggerType.NONE;
		}

		TriggerBuilder(String cronExpression) {
			this.triggerDescription = new TriggerDescription(cronExpression);
			this.triggerType = TriggerType.CRON;
		}

		TriggerBuilder(long period, SchedulingUnit schedulingUnit, boolean fixedRate) {
			this.triggerDescription = new TriggerDescription(period, schedulingUnit, fixedRate);
			this.triggerType = TriggerType.PERIODIC;
		}

		public Trigger build() {
			return new BasicTrigger(triggerType).setTriggerDescription(triggerDescription).setStartDate(startDate).setEndDate(endDate)
					.setRepeatCount(repeatCount);
		}

		public static TriggerBuilder newTrigger() {
			return new TriggerBuilder();
		}

		public static TriggerBuilder newTrigger(String cronExpression) {
			return new TriggerBuilder(cronExpression);
		}

		public static TriggerBuilder newTrigger(long period, SchedulingUnit schedulingUnit, boolean fixedRate) {
			return new TriggerBuilder(period, schedulingUnit, fixedRate);
		}

	}

	public static JobBuilder newJob(JobKey jobKey) {
		return new JobBuilder(jobKey);
	}

	public static JobBuilder newJob(String clusterName, String groupName, String jobName, Class<? extends NotManagedJob> jobClass) {
		return new JobBuilder(clusterName, groupName, jobName, jobClass);
	}

}
