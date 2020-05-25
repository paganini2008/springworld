package com.github.paganini2008.springworld.cluster.multicast;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.github.paganini2008.devtools.StringUtils;
import com.github.paganini2008.springworld.cluster.InstanceId;
import com.github.paganini2008.springworld.redis.pubsub.RedisMessageHandler;

/**
 * 
 * ApplicationMessageListener
 *
 * @author Fred Feng
 * @version 1.0
 */
public class ApplicationMessageListener implements RedisMessageHandler {

	@Autowired
	private InstanceId instanceId;

	@Autowired
	private ClusterMulticastListenerContainer eventListenerContainer;

	@SuppressWarnings("unchecked")
	@Override
	public void onMessage(String channel, Object received) {
		if (received instanceof Map) {
			Map<String, Object> data = (Map<String, Object>) received;
			String instanceId = (String) data.get("instanceId");
			String topic = (String) data.get("topic");
			Object message = data.get("message");
			if (StringUtils.isNotBlank(topic)) {
				eventListenerContainer.fireOnMessage(instanceId, topic, message);
			} else {
				eventListenerContainer.fireOnMessage(instanceId, message);
			}
		}
	}

	@Override
	public String getChannel() {
		return instanceId.get();
	}

}
