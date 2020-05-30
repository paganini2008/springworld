package com.github.paganini2008.springworld.cluster.multicast;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.github.paganini2008.springworld.cluster.ApplicationClusterAware;
import com.github.paganini2008.springworld.cluster.ApplicationInfo;
import com.github.paganini2008.springworld.cluster.InstanceId;
import com.github.paganini2008.springworld.redis.pubsub.RedisMessageHandler;
import com.github.paganini2008.springworld.redis.pubsub.RedisMessageSender;

/**
 * 
 * ApplicationActiveListener
 *
 * @author Fred Feng
 * @version 1.0
 */
public class ApplicationActiveListener implements RedisMessageHandler {

	@Value("${spring.application.name}")
	private String applicationName;

	@Autowired
	private RedisMessageSender redisMessageSender;

	@Autowired
	private ClusterMulticastGroup multicastGroup;

	@Autowired
	private InstanceId instanceId;

	@Autowired
	private ClusterMulticastListenerContainer multicastListenerContainer;

	@Override
	public void onMessage(String channel, Object message) {
		final ApplicationInfo applicationInfo = (ApplicationInfo) message;
		String thatId = applicationInfo.getId();
		int thatWeight = applicationInfo.getWeight();
		if (!multicastGroup.hasRegistered(thatId)) {
			multicastGroup.registerChannel(thatId, thatWeight);
			redisMessageSender.sendMessage(getChannel(), instanceId.getApplicationInfo());
			multicastListenerContainer.fireOnActive(applicationInfo);
		}
	}

	@Override
	public String getChannel() {
		return ApplicationClusterAware.APPLICATION_CLUSTER_NAMESPACE + applicationName + ":active";
	}

}
