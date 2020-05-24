package com.github.paganini2008.springworld.cluster.multicast;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.github.paganini2008.springworld.cluster.ApplicationClusterAware;
import com.github.paganini2008.springworld.cluster.InstanceId;
import com.github.paganini2008.springworld.redis.pubsub.RedisMessageHandler;
import com.github.paganini2008.springworld.redis.pubsub.RedisMessageSender;

/**
 * 
 * ApplicationActiveEventListener
 *
 * @author Fred Feng
 * @version 1.0
 */
public class ApplicationActiveEventListener implements RedisMessageHandler {

	@Value("${spring.application.name}")
	private String applicationName;

	@Autowired
	private RedisMessageSender redisMessageSender;

	@Autowired
	private ClusterMulticastGroup multicastGroup;

	@Autowired
	private InstanceId instanceId;

	@Autowired
	private ClusterMulticastEventListenerContainer eventListenerContainer;

	@Override
	public void onMessage(String channel, Object message) {
		final String[] args = ((String) message).split(":", 2);
		String thatId = args[0];
		int weight = Integer.parseInt(args[1]);
		if (!multicastGroup.hasRegistered(thatId)) {
			multicastGroup.registerChannel(thatId, weight);
			redisMessageSender.sendMessage(getChannel(), instanceId.get() + ":" + instanceId.getWeight());

			eventListenerContainer.fireOnActive(thatId);
		}
	}

	@Override
	public String getChannel() {
		return ApplicationClusterAware.APPLICATION_CLUSTER_NAMESPACE + applicationName + ":active";
	}

}
