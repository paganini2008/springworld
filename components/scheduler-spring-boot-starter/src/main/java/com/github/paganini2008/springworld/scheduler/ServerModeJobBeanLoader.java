package com.github.paganini2008.springworld.scheduler;

import java.lang.reflect.Method;

import org.springframework.beans.factory.annotation.Autowired;

import com.github.paganini2008.devtools.proxy.Aspect;
import com.github.paganini2008.devtools.proxy.JdkProxyFactory;
import com.github.paganini2008.devtools.proxy.ProxyFactory;
import com.github.paganini2008.devtools.reflection.MethodUtils;
import com.github.paganini2008.springworld.cluster.utils.ApplicationContextUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * ServerModeJobBeanLoader
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Slf4j
public class ServerModeJobBeanLoader implements JobBeanLoader {

	@Autowired
	private JobManager jobManager;

	private final ProxyFactory proxyFactory = new JdkProxyFactory();

	@Override
	public Job loadJobBean(JobKey jobKey) throws Exception {
		Job job = ApplicationContextUtils.autowireBean(new JobBeanProxy(jobKey));
		JobTrigger trigger = jobManager.getJobTrigger(job);
		TriggerDescription triggerDescription = trigger.getTriggerDescription();
		switch (trigger.getTriggerType()) {
		case CRON:
			return (Job) proxyFactory.getProxy(job, new CronJobAspect(triggerDescription), CronJob.class);
		case PERIODIC:
			return (Job) proxyFactory.getProxy(job, new PeriodicJobAspect(triggerDescription), PeriodicJob.class);
		case SERIAL:
			return (Job) proxyFactory.getProxy(job, new SerialJobAspect(triggerDescription), SerialJob.class);
		}
		throw new UnsupportedOperationException("Unknown trigger type: " + trigger.getTriggerType().name());
	}

	/**
	 * 
	 * SerialJobAspect
	 * 
	 * @author Fred Feng
	 *
	 * @since 1.0
	 */
	private static class SerialJobAspect implements Aspect {

		private final TriggerDescription data;

		SerialJobAspect(TriggerDescription data) {
			this.data = data;
		}

		@Override
		public Object call(Object target, Method method, Object[] args) {
			final String methodName = method.getName();
			if ("getDependencies".equals(methodName)) {
				return data.getDependencies();
			}
			return MethodUtils.invokeMethod(target, method, args);
		}

		@Override
		public void catchException(Object target, Method method, Object[] args, Throwable e) {
			log.info(e.getMessage(), e);
		}

	}

	/**
	 * 
	 * PeriodicJobAspect
	 * 
	 * @author Fred Feng
	 *
	 * @since 1.0
	 */
	private static class PeriodicJobAspect implements Aspect {

		private final TriggerDescription data;

		PeriodicJobAspect(TriggerDescription data) {
			this.data = data;
		}

		@Override
		public Object call(Object target, Method method, Object[] args) {
			final String methodName = method.getName();
			if ("getDelay".equals(methodName)) {
				return data.getDelay();
			} else if ("getDelaySchedulingUnit".equals(methodName)) {
				return data.getDelaySchedulingUnit();
			} else if ("getPeriod".equals(methodName)) {
				return data.getPeriod();
			} else if ("getPeriodSchedulingUnit".equals(methodName)) {
				return data.getPeriodSchedulingUnit();
			} else if ("getSchedulingMode".equals(methodName)) {
				return data.getSchedulingMode();
			}
			return MethodUtils.invokeMethod(target, method, args);
		}

		@Override
		public void catchException(Object target, Method method, Object[] args, Throwable e) {
			log.info(e.getMessage(), e);
		}
	}

	/**
	 * 
	 * CronJobAspect
	 * 
	 * @author Fred Feng
	 *
	 * @since 1.0
	 */
	private static class CronJobAspect implements Aspect {

		private final TriggerDescription data;

		CronJobAspect(TriggerDescription data) {
			this.data = data;
		}

		@Override
		public Object call(Object target, Method method, Object[] args) {
			final String methodName = method.getName();
			if ("getCronExpression".equals(methodName)) {
				return data.getCron();
			}
			return MethodUtils.invokeMethod(target, method, args);
		}

		@Override
		public void catchException(Object target, Method method, Object[] args, Throwable e) {
			log.info(e.getMessage(), e);
		}

	}
}
