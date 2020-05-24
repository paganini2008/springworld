package com.github.paganini2008.springworld.cluster.multicast;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.github.paganini2008.springworld.cluster.ApplicationClusterAware;
import com.github.paganini2008.springworld.redis.pubsub.RedisMessageHandler;

/**
 * 
 * ApplicationInactiveEventListener
 *
 * @author Fred Feng
 * @version 1.0
 */
public class ApplicationInactiveEventListener implements RedisMessageHandler {

	@Autowired
	private ClusterMulticastGroup multicastGroup;

	@Autowired
	private ClusterMulticastEventListenerContainer eventListenerContainer;

	@Value("${spring.application.name}")
	private String applicationName;

	@Override
	public void onMessage(String channel, Object message) {
		final String instanceId = (String) message;
		multicastGroup.removeChannel(instanceId);
		eventListenerContainer.fireOnInactive(instanceId);
	}

	@Override
	public String getChannel() {
		return ApplicationClusterAware.APPLICATION_CLUSTER_NAMESPACE + applicationName + ":*";
	}

	@Override
	public boolean isEphemeral() {
		return true;
	}

}
