package com.github.paganini2008.springworld.scheduler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.github.paganini2008.devtools.ClassUtils;
import com.github.paganini2008.devtools.beans.BeanUtils;
import com.github.paganini2008.devtools.collection.MapUtils;
import com.github.paganini2008.springworld.cluster.utils.ApplicationContextUtils;

/**
 * 
 * EmbeddedModeJobBeanLoader
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class EmbeddedModeJobBeanLoader implements JobBeanLoader {

	private final Map<String, Job> temporaryJobBeans = new ConcurrentHashMap<String, Job>();

	@Override
	public Job defineJob(JobParameter jobParameter) {
		final String jobClassName = jobParameter.getJobClassName();
		Class<?> jobClass = ClassUtils.forName(jobClassName);
		if (!Job.class.isAssignableFrom(jobClass)) {
			throw new JobException("Class '" + jobClass.getName() + "' is not a implementor of interface " + Job.class.getName());
		}
		final String jobName = jobParameter.getJobName();
		Job job = (Job) ApplicationContextUtils.getBean(jobName, jobClass);
		if (job == null) {
			job = (Job) ApplicationContextUtils.getBean(jobClass, bean -> {
				return ((Job) bean).getJobName().equals(jobName);
			});
		}
		if (job == null) {
			job = MapUtils.get(temporaryJobBeans, jobParameter.getSignature(), () -> {
				return (Job) BeanUtils.instantiate(jobClass);
			});
			if (!job.getSignature().equals(jobParameter.getSignature())) {
				throw new JobBeanNotFoundException(jobParameter.getSignature());
			}
		}
		return job;
	}
}
