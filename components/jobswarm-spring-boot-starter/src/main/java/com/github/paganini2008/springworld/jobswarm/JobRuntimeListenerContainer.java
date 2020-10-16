package com.github.paganini2008.springworld.jobswarm;

import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.github.paganini2008.devtools.ArrayUtils;
import com.github.paganini2008.devtools.Assert;
import com.github.paganini2008.devtools.beans.BeanUtils;
import com.github.paganini2008.springworld.cluster.utils.ApplicationContextUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * JobRuntimeListenerContainer
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Slf4j
public class JobRuntimeListenerContainer {

	private final Set<JobRuntimeListener> globalListeners = Collections.synchronizedNavigableSet(new TreeSet<JobRuntimeListener>());

	private final Map<JobKey, JobRuntimeListener> listeners = Collections.unmodifiableMap(new TreeMap<JobKey, JobRuntimeListener>());

	public void addListener(JobKey jobKey, JobRuntimeListener listener) {
		Assert.isNull(listener, "Nullable JobRuntimeListener");
		if (jobKey != null) {
			listeners.putIfAbsent(jobKey, listener);
			log.info("Add JobRuntimeListener: {} to job: {}", listener, jobKey);
		} else {
			globalListeners.add(listener);
			log.info("Add JobRuntimeListener: {}", listener);
		}
	}

	public void removeListener(JobKey jobKey, JobRuntimeListener listener) {
		Assert.isNull(listener, "Nullable JobRuntimeListener");
		if (jobKey != null) {
			listeners.remove(jobKey, listener);
			log.info("Remove JobRuntimeListener: {} from job: {}", listener, jobKey);
		} else {
			globalListeners.remove(listener);
			log.info("Remove JobRuntimeListener: {}", listener);
		}
	}

	public void beforeRun(long traceId, JobKey jobKey, Job job, Object attachment, Date startDate) {
		for (JobRuntimeListener listener : globalListeners) {
			listener.beforeRun(traceId, jobKey, attachment, startDate);
		}

		if (listeners.containsKey(jobKey)) {
			listeners.get(jobKey).beforeRun(traceId, jobKey, attachment, startDate);
		}

		Class<?>[] listenerClasses = job.getJobRuntimeListeners();
		if (ArrayUtils.isNotEmpty(listenerClasses)) {
			for (Class<?> listenerClass : listenerClasses) {
				JobRuntimeListener listener = (JobRuntimeListener) BeanUtils.instantiate(listenerClass);
				listener.beforeRun(traceId, jobKey, attachment, startDate);
			}
		}
	}

	public void afterRun(long traceId, JobKey jobKey, Job job, Object attachment, Date startDate, RunningState runningState, Object result,
			Throwable reason, int retries) {

		Class<?>[] listenerClasses = job.getJobRuntimeListeners();
		if (ArrayUtils.isNotEmpty(listenerClasses)) {
			for (Class<?> listenerClass : listenerClasses) {
				JobRuntimeListener listener = (JobRuntimeListener) ApplicationContextUtils.instantiateClass(listenerClass);
				listener.afterRun(traceId, jobKey, attachment, startDate, runningState, result, reason);
			}
		}

		if (listeners.containsKey(jobKey)) {
			listeners.get(jobKey).afterRun(traceId, jobKey, attachment, startDate, runningState, result, reason);
		}

		for (JobRuntimeListener listener : globalListeners) {
			listener.afterRun(traceId, jobKey, attachment, startDate, runningState, result, reason);
		}
	}

	public int countOfListeners() {
		return globalListeners.size() + listeners.size();
	}

}
