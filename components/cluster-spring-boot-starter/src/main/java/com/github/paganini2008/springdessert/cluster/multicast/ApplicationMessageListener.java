package com.github.paganini2008.springdessert.cluster.multicast;

import org.springframework.beans.factory.annotation.Autowired;

import com.github.paganini2008.devtools.StringUtils;
import com.github.paganini2008.springdessert.cluster.ApplicationInfo;
import com.github.paganini2008.springdessert.cluster.InstanceId;
import com.github.paganini2008.springdessert.cluster.multicast.ApplicationMulticastGroup.MulticastMessage;
import com.github.paganini2008.springdessert.reditools.messager.RedisMessageHandler;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * ApplicationMessageListener
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
@Slf4j
public class ApplicationMessageListener implements RedisMessageHandler {

	@Autowired
	private InstanceId instanceId;

	@Autowired
	private MulticastListenerContainer multicastListenerContainer;

	@Autowired
	private ApplicationMulticastGroup multicastGroup;

	@Override
	public void onMessage(String channel, Object received) {
		MulticastMessage messageObject = (MulticastMessage) received;
		ApplicationInfo applicationInfo = messageObject.getApplicationInfo();
		String id = messageObject.getId();
		String topic = messageObject.getTopic();
		Object message = messageObject.getMessage();
		boolean ack = messageObject.getTimeout() > 0;
		try {
			if (StringUtils.isNotBlank(topic)) {
				multicastListenerContainer.fireOnMessage(applicationInfo, topic, id, message);
				if (ack) {
					multicastGroup.ack(applicationInfo.getId(), messageObject);
				}
			} else {
				multicastListenerContainer.fireOnMessage(applicationInfo, message);
			}
		} catch (Throwable e) {
			log.error("Failed to send MulticastMessage '{}'", id, e);
		}

	}

	@Override
	public String getChannel() {
		return instanceId.get();
	}

}
