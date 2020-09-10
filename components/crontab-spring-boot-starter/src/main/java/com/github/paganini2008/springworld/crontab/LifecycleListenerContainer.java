package com.github.paganini2008.springworld.crontab;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import com.github.paganini2008.springworld.cluster.ApplicationClusterAware;
import com.github.paganini2008.springworld.crontab.model.JobActionParam;
import com.github.paganini2008.springworld.redisplus.messager.RedisMessageHandler;
import com.github.paganini2008.springworld.redisplus.messager.RedisMessageSender;

/**
 * 
 * LifecycleListenerContainer
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class LifecycleListenerContainer implements RedisMessageHandler {

	private final Set<LifecycleListener> lifecycleListeners = Collections.synchronizedNavigableSet(new TreeSet<LifecycleListener>());

	private final String clusterName;
	private final RedisMessageSender redisMessageSender;

	public LifecycleListenerContainer(String clusterName, RedisMessageSender redisMessageSender) {
		this.clusterName = clusterName;
		this.redisMessageSender = redisMessageSender;
	}

	public void addListener(LifecycleListener listener) {
		if (listener != null) {
			lifecycleListeners.add(listener);
		}
	}

	public void removeListener(LifecycleListener listener) {
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
			for (LifecycleListener listener : lifecycleListeners) {
				listener.afterCreation(jobKey);
			}
			break;
		case DELETION:
			for (LifecycleListener listener : lifecycleListeners) {
				listener.beforeDeletion(jobKey);
			}
			break;
		case REFRESH:
			for (LifecycleListener listener : lifecycleListeners) {
				listener.afterRefresh(jobKey);
			}
			break;
		}
	}

	@Override
	public String getChannel() {
		return ApplicationClusterAware.APPLICATION_CLUSTER_NAMESPACE + clusterName + ":crontab:job:action";
	}

}
