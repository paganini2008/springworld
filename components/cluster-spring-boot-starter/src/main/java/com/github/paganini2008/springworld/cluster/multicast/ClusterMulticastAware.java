package com.github.paganini2008.springworld.cluster.multicast;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import com.github.paganini2008.springworld.cluster.ApplicationClusterAware;
import com.github.paganini2008.springworld.cluster.InstanceId;
import com.github.paganini2008.springworld.redis.pubsub.RedisMessageSender;

/**
 * 
 * ClusterMulticastAware
 *
 * @author Fred Feng
 * @version 1.0
 */
public class ClusterMulticastAware implements ApplicationListener<ContextRefreshedEvent> {

	@Value("${spring.application.name}")
	private String applicationName;

	@Autowired
	private RedisMessageSender redisMessageSender;

	@Autowired
	private InstanceId instanceId;

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		final String channel = ApplicationClusterAware.APPLICATION_CLUSTER_NAMESPACE + applicationName + ":active";
		redisMessageSender.sendMessage(channel, instanceId.get() + ":" + instanceId.getWeight());
	}

}
