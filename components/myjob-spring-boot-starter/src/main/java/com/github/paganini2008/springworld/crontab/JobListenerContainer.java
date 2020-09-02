package com.github.paganini2008.springworld.crontab;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import com.github.paganini2008.springworld.cluster.ApplicationInfo;
import com.github.paganini2008.springworld.cluster.multicast.ClusterMessageListener;

/**
 * 
 * JobListenerContainer
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class JobListenerContainer implements ClusterMessageListener {

	public static final String TOPIC_NAME = "jobListenerContainer";

	private final Set<JobListener> jobListeners = Collections.synchronizedNavigableSet(new TreeSet<JobListener>());

	public void addListener(JobListener jobListener) {
		if (jobListener != null) {
			jobListeners.add(jobListener);
		}
	}

	public void removeListener(JobListener jobListener) {
		if (jobListener != null) {
			jobListeners.remove(jobListener);
		}
	}

	@Override
	public void onMessage(ApplicationInfo applicationInfo, Object message) {
		final JobParam jobParam = (JobParam) message;
		final JobKey jobKey = jobParam.getJobKey();
		final JobAction jobAction = (JobAction) jobParam.getAttachment();
		switch (jobAction) {
		case CREATION:
			for (JobListener jobListener : jobListeners) {
				jobListener.afterCreation(jobKey);
			}
			break;
		case DELETION:
			for (JobListener jobListener : jobListeners) {
				jobListener.beforeDeletion(jobKey);
			}
			break;
		case REFRESH:
			for (JobListener jobListener : jobListeners) {
				jobListener.afterRefresh(jobKey);
			}
			break;
		}

	}

	@Override
	public String getTopic() {
		return TOPIC_NAME;
	}

}
