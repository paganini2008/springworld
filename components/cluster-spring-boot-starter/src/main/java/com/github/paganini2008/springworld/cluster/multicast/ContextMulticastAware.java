package com.github.paganini2008.springworld.cluster.multicast;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import com.github.paganini2008.springworld.cluster.ClusterId;
import com.github.paganini2008.springworld.redis.pubsub.RedisMessageSender;

/**
 * 
 * ContextMulticastAware
 *
 * @author Fred Feng
 * @version 1.0
 */
public class ContextMulticastAware implements ApplicationListener<ContextRefreshedEvent> {

	@Autowired
	private RedisMessageSender redisMessageSender;

	@Autowired
	private ClusterId clusterId;

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		redisMessageSender.sendMessage(ContextMulticastEventNames.STANDBY, clusterId.get() + ":" + clusterId.getWeight());
	}

}
