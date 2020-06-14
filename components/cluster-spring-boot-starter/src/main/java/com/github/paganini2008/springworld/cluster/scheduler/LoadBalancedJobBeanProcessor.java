package com.github.paganini2008.springworld.cluster.scheduler;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.github.paganini2008.springworld.cluster.ApplicationInfo;
import com.github.paganini2008.springworld.cluster.multicast.ClusterMessageListener;
import com.github.paganini2008.springworld.cluster.utils.ApplicationContextUtils;

/**
 * 
 * LoadBalancedJobBeanProcessor
 *
 * @author Fred Feng
 * @since 1.0
 */
public class LoadBalancedJobBeanProcessor implements ClusterMessageListener {

	private static final Map<String, Class<?>> jobClassCache = Collections.synchronizedMap(new HashMap<String, Class<?>>());

	private static Class<?> getJobClassIfAvailable(String className) {
		Class<?> type = jobClassCache.get(className);
		if (type == null) {
			Class<?> jobClass;
			try {
				jobClass = Class.forName(className);
			} catch (ClassNotFoundException e) {
				throw new ClusterSchedulingException(e.getMessage(), e);
			}
			if (!Job.class.isAssignableFrom(jobClass)) {
				throw new ClusterSchedulingException(jobClass.getName() + " is not a job instance.");
			}
			jobClassCache.put(className, jobClass);
			type = jobClassCache.get(className);
		}
		return type;
	}

	@Override
	public void onMessage(ApplicationInfo applicationInfo, Object message) {
		String[] data = ((String) message).split("@", 2);
		final String jobName = data[0];
		final String jobClassName = data[1];
		Class<?> jobClass = getJobClassIfAvailable(jobClassName);
		Job job = (Job) ApplicationContextUtils.getBean(jobName, jobClass);
		if (job == null) {
			job = (Job) ApplicationContextUtils.getBean(jobClass, bean -> {
				return ((Job) bean).getName().equals(jobName);
			});
		}
		if (job == null) {
			throw new ClusterSchedulingException("Undefined job bean by class: " + jobClass);
		}
		new DefaultJobBeanProxy(job).run();
	}

}
