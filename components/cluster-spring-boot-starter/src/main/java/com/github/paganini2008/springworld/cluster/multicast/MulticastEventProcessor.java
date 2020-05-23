package com.github.paganini2008.springworld.cluster.multicast;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.github.paganini2008.springworld.cluster.ClusterId;
import com.github.paganini2008.springworld.redis.pubsub.RedisMessageHandler;

/**
 * 
 * MulticastEventProcessor
 *
 * @author Fred Feng
 * @version 1.0
 */
public class MulticastEventProcessor implements RedisMessageHandler {

	@Autowired
	private ClusterId clusterId;

	@Autowired
	private ContextMulticastEventListener multicastEventListener;

	@SuppressWarnings("unchecked")
	@Override
	public void onMessage(String channel, Object received) {
		if (received instanceof Map) {
			Map<String, Object> data = (Map<String, Object>) received;
			String instanceId = (String) data.get("instanceId");
			String topic = (String) data.get("topic");
			Object message = data.get("message");
			multicastEventListener.fireOnMessage(instanceId, topic, message);
		}
	}

	@Override
	public String getChannel() {
		return clusterId.get();
	}

}
