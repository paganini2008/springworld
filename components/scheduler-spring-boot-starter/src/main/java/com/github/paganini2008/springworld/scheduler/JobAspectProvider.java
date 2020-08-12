package com.github.paganini2008.springworld.scheduler;

import java.lang.reflect.Method;

import com.github.paganini2008.devtools.proxy.Aspect;
import com.github.paganini2008.devtools.reflection.MethodUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * JobAspectProvider
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Slf4j
public abstract class JobAspectProvider {

	public static Aspect grantPeriodicJob(TriggerDescription triggerDescription) {
		return new PeriodicJobAspect(triggerDescription);
	}

	public static Aspect grantCronJob(TriggerDescription triggerDescription) {
		return new CronJobAspect(triggerDescription);
	}

	public static Aspect grantSerialJob(TriggerDescription triggerDescription) {
		return new SerialJobAspect(triggerDescription);
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

		private final TriggerDescription triggerDescription;

		SerialJobAspect(TriggerDescription triggerDescription) {
			this.triggerDescription = triggerDescription;
		}

		@Override
		public Object call(Object target, Method method, Object[] args) {
			final String methodName = method.getName();
			if ("getDependencies".equals(methodName)) {
				return triggerDescription.getDependencies();
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

		private final TriggerDescription triggerDescription;

		PeriodicJobAspect(TriggerDescription triggerDescription) {
			this.triggerDescription = triggerDescription;
		}

		@Override
		public Object call(Object target, Method method, Object[] args) {
			final String methodName = method.getName();
			if ("getDelay".equals(methodName)) {
				return triggerDescription.getDelay();
			} else if ("getDelaySchedulingUnit".equals(methodName)) {
				return triggerDescription.getDelaySchedulingUnit();
			} else if ("getPeriod".equals(methodName)) {
				return triggerDescription.getPeriod();
			} else if ("getPeriodSchedulingUnit".equals(methodName)) {
				return triggerDescription.getPeriodSchedulingUnit();
			} else if ("getSchedulingMode".equals(methodName)) {
				return triggerDescription.getSchedulingMode();
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

		private final TriggerDescription triggerDescription;

		CronJobAspect(TriggerDescription triggerDescription) {
			this.triggerDescription = triggerDescription;
		}

		@Override
		public Object call(Object target, Method method, Object[] args) {
			final String methodName = method.getName();
			if ("getCronExpression".equals(methodName)) {
				return triggerDescription.getCron();
			}
			return MethodUtils.invokeMethod(target, method, args);
		}

		@Override
		public void catchException(Object target, Method method, Object[] args, Throwable e) {
			log.info(e.getMessage(), e);
		}

	}

}
