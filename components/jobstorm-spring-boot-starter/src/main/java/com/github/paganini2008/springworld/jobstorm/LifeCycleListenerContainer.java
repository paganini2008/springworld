package com.github.paganini2008.springworld.jobstorm;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import org.springframework.beans.factory.annotation.Value;

import com.github.paganini2008.springworld.cluster.ApplicationInfo;
import com.github.paganini2008.springworld.cluster.multicast.ClusterMessageListener;
import com.github.paganini2008.springworld.jobstorm.model.JobLifeCycleParam;

/**
 * 
 * LifeCycleListenerContainer
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public abstract class LifeCycleListenerContainer implements ClusterMessageListener {

	private final Set<LifeCycleListener> lifeCycleListeners = Collections.synchronizedNavigableSet(new TreeSet<LifeCycleListener>());

	@Value("${spring.application.cluster.name}")
	private String clusterName;

	public void addListener(LifeCycleListener listener) {
		if (listener != null) {
			lifeCycleListeners.add(listener);
		}
	}

	public void removeListener(LifeCycleListener listener) {
		if (listener != null) {
			lifeCycleListeners.remove(listener);
		}
	}

	public abstract void onChange(JobKey jobKey, JobLifeCycle lifeCycle);

	@Override
	public void onMessage(ApplicationInfo applicationInfo, String id, Object message) {
		final JobLifeCycleParam jobParam = (JobLifeCycleParam) message;
		JobKey jobKey = jobParam.getJobKey();
		JobLifeCycle lifeCycle = jobParam.getLifeCycle();
		accept(jobKey, lifeCycle);
	}

	private void accept(JobKey jobKey, JobLifeCycle lifeCycle) {
		switch (lifeCycle) {
		case CREATION:
			for (LifeCycleListener listener : lifeCycleListeners) {
				listener.afterCreation(jobKey);
			}
			break;
		case DELETION:
			for (LifeCycleListener listener : lifeCycleListeners) {
				listener.beforeDeletion(jobKey);
			}
			break;
		case REFRESH:
			for (LifeCycleListener listener : lifeCycleListeners) {
				listener.afterRefresh(jobKey);
			}
			break;
		}
	}

	public int countOfListeners() {
		return lifeCycleListeners.size();
	}

	@Override
	public String getTopic() {
		return LifeCycleListenerContainer.class.getName();
	}

}
