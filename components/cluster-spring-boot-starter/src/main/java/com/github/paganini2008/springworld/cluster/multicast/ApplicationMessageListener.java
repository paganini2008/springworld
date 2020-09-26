package com.github.paganini2008.springworld.cluster.multicast;

import org.springframework.beans.factory.annotation.Autowired;

import com.github.paganini2008.devtools.StringUtils;
import com.github.paganini2008.springworld.cluster.ApplicationInfo;
import com.github.paganini2008.springworld.cluster.InstanceId;
import com.github.paganini2008.springworld.cluster.multicast.ClusterMulticastGroup.ClusterMulticastMessage;
import com.github.paganini2008.springworld.redisplus.messager.RedisMessageHandler;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * ApplicationMessageListener
 *
 * @author Fred Feng
 * @version 1.0
 */
@Slf4j
public class ApplicationMessageListener implements RedisMessageHandler {

	@Autowired
	private InstanceId instanceId;

	@Autowired
	private ClusterMulticastListenerContainer multicastListenerContainer;

	@Autowired
	private ClusterMulticastGroup clusterMulticastGroup;

	@Override
	public void onMessage(String channel, Object received) {
		ClusterMulticastMessage messageObject = (ClusterMulticastMessage) received;
		ApplicationInfo applicationInfo = messageObject.getApplicationInfo();
		String id = messageObject.getId();
		String topic = messageObject.getTopic();
		Object message = messageObject.getMessage();
		boolean ack = messageObject.getTimeout() > 0;
		try {
			if (StringUtils.isNotBlank(topic)) {
				multicastListenerContainer.fireOnMessage(applicationInfo, topic, id, message);
				if (ack) {
					clusterMulticastGroup.ack(applicationInfo.getId(), messageObject);
				}
			} else {
				multicastListenerContainer.fireOnMessage(applicationInfo, message);
			}
		} catch (Throwable e) {
			log.error("Failed to send clusterMulticastMessage '{}'", id, e);
		}

	}

	@Override
	public String getChannel() {
		return instanceId.get();
	}

}
