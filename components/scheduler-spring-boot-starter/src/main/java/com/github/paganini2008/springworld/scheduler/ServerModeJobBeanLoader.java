package com.github.paganini2008.springworld.scheduler;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.ObjectMapper;
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

	private final ObjectMapper objectMapper = new ObjectMapper();
	private final ProxyFactory proxyFactory = new JdkProxyFactory();

	@SuppressWarnings("unchecked")
	@Override
	public Job defineJob(JobParameter jobParameter) throws Exception {
		Job job = ApplicationContextUtils.autowireBean(new JobBeanProxy(jobParameter));
		TriggerDetail triggerDetail = jobManager.getTriggerDetail(job);
		Map<String, Object> data;
		try {
			data = objectMapper.readValue(triggerDetail.getTriggerDescription(), HashMap.class);
		} catch (IOException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
		switch (triggerDetail.getTriggerType()) {
		case CRON:
			return (Job) proxyFactory.getProxy(job, new CronJobAspect(data), CronJob.class);
		case PERIODIC:
			return (Job) proxyFactory.getProxy(job, new PeriodicJobAspect(data), PeriodicJob.class);
		}
		return null;
	}

	/**
	 * 
	 * PeriodicJobAspect
	 * 
	 * @author Fred Feng
	 *
	 * @since 1.0
	 */
	static class PeriodicJobAspect implements Aspect {

		private final Map<String, Object> data;

		PeriodicJobAspect(Map<String, Object> data) {
			this.data = data;
		}

		@Override
		public Object call(Object target, Method method, Object[] args) {
			final String methodName = method.getName();
			if ("getDelay".equals(methodName)) {
				return Long.parseLong((String) data.get("delay"));
			} else if ("getDelayTimeUnit".equals(methodName)) {
				return TimeUnit.valueOf((String) data.get("delayTimeUnit"));
			} else if ("getPeriod".equals(methodName)) {
				return Long.parseLong((String) data.get("period"));
			} else if ("getPeriodTimeUnit".equals(methodName)) {
				return TimeUnit.valueOf((String) data.get("periodTimeUnit"));
			} else if ("getSchedulingMode".equals(methodName)) {
				return SchedulingMode.valueOf((String) data.get("schedulingMode"));
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
	static class CronJobAspect implements Aspect {

		private final Map<String, Object> data;

		CronJobAspect(Map<String, Object> data) {
			this.data = data;
		}

		@Override
		public Object call(Object target, Method method, Object[] args) {
			final String methodName = method.getName();
			if ("getCronExpression".equals(methodName)) {
				return (String) data.get("cron");
			}
			return MethodUtils.invokeMethod(target, method, args);
		}

		@Override
		public void catchException(Object target, Method method, Object[] args, Throwable e) {
			log.info(e.getMessage(), e);
		}

	}
}
