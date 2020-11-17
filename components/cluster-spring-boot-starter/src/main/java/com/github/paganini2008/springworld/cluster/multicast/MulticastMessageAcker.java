package com.github.paganini2008.springworld.cluster.multicast;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

import com.github.paganini2008.springworld.cluster.ApplicationInfo;
import com.github.paganini2008.springworld.cluster.multicast.ClusterMulticastGroup.ClusterMulticastMessage;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * MulticastMessageAcker
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Slf4j
public class MulticastMessageAcker implements MulticastMessageListener {

	static final String TOPIC_NAME = "<MULTICAST-MESSAGE-ACKER>";

	private final Map<String, ClusterMulticastMessage> ackQueue = new ConcurrentHashMap<String, ClusterMulticastMessage>();

	public void waitForAck(ClusterMulticastMessage message) {
		ackQueue.put(message.getId(), message);
	}

	public void retrySendMessage(ClusterMulticastGroup clusterMulticastGroup) {
		if (ackQueue.isEmpty()) {
			return;
		}
		final Queue<ClusterMulticastMessage> q = new ArrayDeque<ClusterMulticastMessage>(ackQueue.values());
		while (!q.isEmpty()) {
			ClusterMulticastMessage message = q.poll();
			if (System.currentTimeMillis() - message.getTimestamp() > message.getTimeout() * 1000) {
				ackQueue.remove(message.getId());
				clusterMulticastGroup.doSendMessage(message.getChannel(), message, message.getTimeout());
				if (log.isTraceEnabled()) {
					log.trace("Resend clusterMulticastMessage '{}'", message.getId());
				}
			}
		}
	}

	@Override
	public void onMessage(ApplicationInfo applicationInfo, String id, Object obj) {
		if (ackQueue.remove(id) != null) {
			if (log.isTraceEnabled()) {
				log.trace("Acknowledge clusterMulticastMessage '{}'", id);
			}
		}
	}

	@Override
	public String getTopic() {
		return TOPIC_NAME;
	}

}
