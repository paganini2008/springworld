package com.github.paganini2008.springworld.cluster.multicast;

import org.springframework.beans.factory.annotation.Autowired;

import com.github.paganini2008.devtools.StringUtils;
import com.github.paganini2008.springworld.cluster.ApplicationInfo;
import com.github.paganini2008.springworld.cluster.InstanceId;
import com.github.paganini2008.springworld.cluster.multicast.ClusterMulticastGroup.ClusterMulticastMessage;
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
	private ClusterMulticastListenerContainer multicastListenerContainer;

	@Override
	public void onMessage(String channel, Object received) {
		ClusterMulticastMessage data = (ClusterMulticastMessage) received;
		ApplicationInfo info = data.getApplicationInfo();
		String topic = data.getTopic();
		Object message = data.getMessage();
		if (StringUtils.isNotBlank(topic)) {
			multicastListenerContainer.fireOnMessage(info, topic, message);
		} else {
			multicastListenerContainer.fireOnMessage(info, message);
		}

	}

	@Override
	public String getChannel() {
		return instanceId.get();
	}

}
