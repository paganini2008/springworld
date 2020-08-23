package com.github.paganini2008.springworld.myjob;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.github.paganini2008.devtools.ClassUtils;
import com.github.paganini2008.devtools.beans.BeanUtils;
import com.github.paganini2008.devtools.collection.MapUtils;
import com.github.paganini2008.springworld.cluster.utils.ApplicationContextUtils;

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

	private final Map<JobKey, Job> notManagedJobBeans = new ConcurrentHashMap<JobKey, Job>();

	@Override
	public Job loadJobBean(JobKey jobKey) {
		final String jobClassName = jobKey.getJobClassName();
		Class<?> jobClass;
		try {
			jobClass = ClassUtils.forName(jobClassName);
		} catch (RuntimeException e) {
			log.warn("Can not load JobClass by name '" + jobClassName + "' into job instance.");
			return null;
		}
		if (!Job.class.isAssignableFrom(jobClass)) {
			throw new JobException("Class '" + jobClass.getName() + "' is not a instance of interface '" + Job.class.getName() + "'.");
		}
		final String jobName = jobKey.getJobName();
		Job job = (Job) ApplicationContextUtils.getBean(jobName, jobClass);
		if (job == null) {
			job = (Job) ApplicationContextUtils.getBean(jobClass, bean -> {
				return ((Job) bean).getJobName().equals(jobName);
			});
		}
		if (job == null) {
			job = MapUtils.get(notManagedJobBeans, jobKey, () -> {
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
