package com.github.paganini2008.springworld.scheduler;

import org.springframework.beans.factory.annotation.Autowired;

import com.github.paganini2008.springworld.cluster.ApplicationClusterAware;
import com.github.paganini2008.springworld.redisplus.messager.RedisMessageHandler;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * JobDependencyProcessor
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Slf4j
public class JobDependencyProcessor implements RedisMessageHandler {

	public static final String BEAN_NAME = "jobDependencyProcessor";

	private final String clusterName;

	public JobDependencyProcessor(String clusterName) {
		this.clusterName = clusterName;
	}

	@Autowired
	private JobDependencyObservable jobDependencyObservable;

	@Override
	public String getChannel() {
		return ApplicationClusterAware.APPLICATION_CLUSTER_NAMESPACE + clusterName + ":scheduler:job:dependency:*";
	}

	@Override
	public void onMessage(String channel, Object message) throws Exception {
		JobParam jobParam = (JobParam) message;
		log.info("Dependent Job: " + jobParam.getJobKey());
		jobDependencyObservable.executeDependency(jobParam.getJobKey(), jobParam.getAttachment());
	}

}
