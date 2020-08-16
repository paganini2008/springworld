package com.github.paganini2008.springworld.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

import com.github.paganini2008.devtools.Observable;
import com.github.paganini2008.springworld.cluster.ApplicationClusterAware;
import com.github.paganini2008.springworld.redisplus.messager.RedisMessageSender;

/**
 * 
 * JobDependencyObservable
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class JobDependencyObservable extends Observable {

	public JobDependencyObservable() {
		super(true);
	}

	@Autowired
	private RedisMessageSender redisMessageSender;

	@Qualifier("main-job-executor")
	@Autowired
	private JobExecutor jobExecutor;

	@Value("${spring.application.cluster.name}")
	private String clusterName;

	public void addDependency(final Job job, final String[] dependencies) {
		for (String dependency : dependencies) {
			addObserver(dependency, (ob, attachment) -> {
				jobExecutor.execute(job, attachment);
			});
		}
	}

	public void executeDependency(JobKey jobKey, Object attachment) {
		notifyObservers(jobKey.getIdentifier(), attachment);
	}

	public void notifyDependencies(JobKey jobKey, Object result) {
		final String channel = ApplicationClusterAware.APPLICATION_CLUSTER_NAMESPACE + clusterName + ":scheduler:job:dependency:"
				+ jobKey.getIdentifier();
		JobParam jobParam = new JobParam(jobKey, result);
		redisMessageSender.sendMessage(channel, jobParam);
	}

}
