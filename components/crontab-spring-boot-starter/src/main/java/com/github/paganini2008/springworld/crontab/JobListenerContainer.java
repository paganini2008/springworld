package com.github.paganini2008.springworld.crontab;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import com.github.paganini2008.devtools.Observable;
import com.github.paganini2008.springworld.cluster.ApplicationClusterAware;
import com.github.paganini2008.springworld.redisplus.messager.RedisMessageHandler;
import com.github.paganini2008.springworld.redisplus.messager.RedisMessageSender;

/**
 * 
 * JobListenerContainer
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class JobListenerContainer extends Observable implements RedisMessageHandler {

	private final Set<JobListener> jobListeners = Collections.synchronizedNavigableSet(new TreeSet<JobListener>());

	private final String clusterName;
	private final RedisMessageSender redisMessageSender;

	public JobListenerContainer(String clusterName, RedisMessageSender redisMessageSender) {
		super(Boolean.FALSE);
		this.clusterName = clusterName;
		this.redisMessageSender = redisMessageSender;
	}

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

	public void signalAll(JobKey jobKey, JobAction jobAction) {
		super.notifyObservers(jobKey.getIndentifier(), jobAction);
		final String channel = getChannel();
		redisMessageSender.sendMessage(channel, new JobParam(jobKey, jobAction));
	}

	@Override
	public void onMessage(String channel, Object message) throws Exception {
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
	public String getChannel() {
		return ApplicationClusterAware.APPLICATION_CLUSTER_NAMESPACE + clusterName + ":crontab:job:action";
	}

}
