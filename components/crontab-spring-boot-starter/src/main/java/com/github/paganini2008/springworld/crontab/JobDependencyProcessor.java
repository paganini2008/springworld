package com.github.paganini2008.springworld.crontab;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

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

	@Value("${spring.application.cluster.name}")
	private String clusterName;

	@Autowired
	private JobDependencyObservable jobDependencyObservable;

	@Override
	public String getChannel() {
		return ApplicationClusterAware.APPLICATION_CLUSTER_NAMESPACE + clusterName + ":scheduler:job:dependency:*";
	}

	@Override
	public void onMessage(String channel, Object message) throws Exception {
		final JobParam jobParam = (JobParam) message;
		if (log.isTraceEnabled()) {
			log.trace("Dependent Job: " + jobParam.getJobKey());
		}
		jobDependencyObservable.executeDependency(jobParam.getJobKey(), jobParam.getAttachment());
	}

}
