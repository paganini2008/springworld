package com.github.paganini2008.springworld.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
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

	@Autowired
	private JobExecutor jobExecutor;

	@Value("${spring.application.cluster.name}")
	private String clusterName;

	public void addDependency(final SerialJob job) {
		for (String dependency : job.getDependencies()) {
			addObserver(dependency, (ob, attachment) -> {
				jobExecutor.execute(job, attachment);
			});
		}
	}

	public void executeDependency(String signature, Object attachment) {
		notifyObservers(signature, attachment);
	}

	public void notifyDependencies(Job job, Object result) {
		final String channel = ApplicationClusterAware.APPLICATION_CLUSTER_NAMESPACE + clusterName + ":scheduler:job:dependency:"
				+ job.getSignature();
		JobParam jobParam = new JobParam(JobKey.of(job.getSignature()), result);
		redisMessageSender.sendMessage(channel, jobParam);
	}

}
