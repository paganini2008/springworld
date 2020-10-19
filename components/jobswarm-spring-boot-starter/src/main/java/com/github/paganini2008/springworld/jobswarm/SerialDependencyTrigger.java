package com.github.paganini2008.springworld.jobswarm;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.github.paganini2008.springworld.cluster.ApplicationClusterAware;
import com.github.paganini2008.springworld.jobswarm.model.JobParam;
import com.github.paganini2008.springworld.reditools.messager.RedisMessageHandler;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * SerialDependencyTrigger
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Slf4j
public class SerialDependencyTrigger implements RedisMessageHandler {

	static final String BEAN_NAME = "serialDependencyTrigger";

	@Value("${spring.application.cluster.name}")
	private String clusterName;

	@Autowired
	private SerialDependencyScheduler serialDependencyScheduler;

	@Override
	public String getChannel() {
		return ApplicationClusterAware.APPLICATION_CLUSTER_NAMESPACE + clusterName + ":scheduler:job:dependency:*";
	}

	@Override
	public void onMessage(String channel, Object message) throws Exception {
		final JobParam jobParam = (JobParam) message;
		if (log.isTraceEnabled()) {
			log.trace("Trigger all serial dependencies by Job: " + jobParam.getJobKey());
		}
		serialDependencyScheduler.triggerDependency(jobParam.getJobKey(), jobParam.getAttachment());
	}

}
