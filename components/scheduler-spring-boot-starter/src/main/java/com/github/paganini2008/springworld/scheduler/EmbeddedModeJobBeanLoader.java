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

	private final Map<JobKey, Job> temporaryJobBeans = new ConcurrentHashMap<JobKey, Job>();

	@Override
	public Job loadJobBean(JobKey jobKey) {
		final String jobClassName = jobKey.getJobClassName();
		Class<?> jobClass = ClassUtils.forName(jobClassName);
		if (!Job.class.isAssignableFrom(jobClass)) {
			throw new JobException("Class '" + jobClass.getName() + "' is not a implementor of interface " + Job.class.getName());
		}
		final String jobName = jobKey.getJobName();
		Job job = (Job) ApplicationContextUtils.getBean(jobName, jobClass);
		if (job == null) {
			job = (Job) ApplicationContextUtils.getBean(jobClass, bean -> {
				return ((Job) bean).getJobName().equals(jobName);
			});
		}
		if (job == null) {
			job = MapUtils.get(temporaryJobBeans, jobKey, () -> {
				return (Job) BeanUtils.instantiate(jobClass);
			});
		}
		if (job != null) {
			if (!JobKey.of(job).equals(jobKey)) {
				throw new JobBeanNotFoundException(jobKey.toString());
			}
		}
		return job;
	}
}
