package com.github.paganini2008.springworld.jobclick;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import com.github.paganini2008.springworld.cluster.ApplicationClusterAware;
import com.github.paganini2008.springworld.jobclick.model.JobActionParam;
import com.github.paganini2008.springworld.reditools.messager.RedisMessageHandler;
import com.github.paganini2008.springworld.reditools.messager.RedisMessageSender;

/**
 * 
 * LifeCycleListenerContainer
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class LifeCycleListenerContainer implements RedisMessageHandler {

	private final Set<LifeCycleListener> lifecycleListeners = Collections.synchronizedNavigableSet(new TreeSet<LifeCycleListener>());

	private final String clusterName;
	private final RedisMessageSender redisMessageSender;

	public LifeCycleListenerContainer(String clusterName, RedisMessageSender redisMessageSender) {
		this.clusterName = clusterName;
		this.redisMessageSender = redisMessageSender;
	}

	public void addListener(LifeCycleListener listener) {
		if (listener != null) {
			lifecycleListeners.add(listener);
		}
	}

	public void removeListener(LifeCycleListener listener) {
		if (listener != null) {
			lifecycleListeners.remove(listener);
		}
	}

	public void signalAll(JobKey jobKey, JobAction jobAction) {
		final String channel = getChannel();
		redisMessageSender.sendMessage(channel, new JobActionParam(jobKey, jobAction));
	}

	@Override
	public void onMessage(String channel, Object message) throws Exception {
		final JobActionParam jobParam = (JobActionParam) message;
		JobKey jobKey = jobParam.getJobKey();
		JobAction jobAction = jobParam.getAction();
		accept(jobKey, jobAction);
	}

	private void accept(JobKey jobKey, JobAction jobAction) {
		switch (jobAction) {
		case CREATION:
			for (LifeCycleListener listener : lifecycleListeners) {
				listener.afterCreation(jobKey);
			}
			break;
		case DELETION:
			for (LifeCycleListener listener : lifecycleListeners) {
				listener.beforeDeletion(jobKey);
			}
			break;
		case REFRESH:
			for (LifeCycleListener listener : lifecycleListeners) {
				listener.afterRefresh(jobKey);
			}
			break;
		}
	}

	@Override
	public String getChannel() {
		return ApplicationClusterAware.APPLICATION_CLUSTER_NAMESPACE + clusterName + ":joblink:job:action";
	}

}
