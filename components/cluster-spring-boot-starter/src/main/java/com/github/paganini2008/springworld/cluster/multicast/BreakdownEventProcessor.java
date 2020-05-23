package com.github.paganini2008.springworld.cluster.multicast;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.github.paganini2008.springworld.cluster.ContextClusterAware;
import com.github.paganini2008.springworld.redis.pubsub.RedisMessageHandler;

/**
 * 
 * BreakdownEventProcessor
 *
 * @author Fred Feng
 * @version 1.0
 */
public class BreakdownEventProcessor implements RedisMessageHandler {

	@Autowired
	private ContextMulticastGroup multicastGroup;

	@Autowired
	private ContextMulticastEventListener multicastEventListener;

	@Value("${spring.application.name}")
	private String applicationName;

	@Override
	public void onMessage(String channel, Object message) {
		final String clusterId = (String) message;
		multicastGroup.removeChannel(clusterId);
		multicastEventListener.fireOnLeave(clusterId);
	}

	@Override
	public String getChannel() {
		return ContextClusterAware.SPRING_CLUSTER_NAMESPACE + applicationName + ":*";
	}

	@Override
	public boolean isEphemeral() {
		return true;
	}

}
