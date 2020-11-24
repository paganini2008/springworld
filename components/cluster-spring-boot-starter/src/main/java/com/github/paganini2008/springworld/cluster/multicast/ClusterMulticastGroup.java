package com.github.paganini2008.springworld.cluster.multicast;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.github.paganini2008.devtools.Assert;
import com.github.paganini2008.devtools.StringUtils;
import com.github.paganini2008.devtools.collection.MapUtils;
import com.github.paganini2008.devtools.multithreads.Executable;
import com.github.paganini2008.devtools.multithreads.ThreadUtils;
import com.github.paganini2008.springdessert.reditools.messager.RedisMessageSender;
import com.github.paganini2008.springworld.cluster.ApplicationInfo;
import com.github.paganini2008.springworld.cluster.InstanceId;
import com.github.paganini2008.springworld.cluster.utils.LoadBalancer;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * ClusterMulticastGroup
 *
 * @author Fred Feng
 * @version 1.0
 */
@Slf4j
public class ClusterMulticastGroup {

	private final List<String> allChannels = new CopyOnWriteArrayList<String>();
	private final Map<String, List<String>> groupChannels = new ConcurrentHashMap<String, List<String>>();

	@Autowired
	private RedisMessageSender redisMessageSender;

	@Qualifier("multicastLoadBalancer")
	@Autowired
	private LoadBalancer<String> loadBalancer;

	@Autowired
	private InstanceId instanceId;

	@Autowired
	private MulticastMessageAcker clusterMulticastMessageAcker;

	private ClusterMulticastMessageAckerChecker clusterMulticastMessageAckerChecker;

	@PostConstruct
	public void configure() {
		clusterMulticastMessageAckerChecker = new ClusterMulticastMessageAckerChecker();
		clusterMulticastMessageAckerChecker.start();
	}

	public void registerChannel(String group, String channel, int weight) {
		Assert.hasNoText(channel, "Channel must be required.");
		Assert.lt(weight, 0, "Weight must be > 0");

		for (int i = 0; i < weight; i++) {
			allChannels.add(channel);
		}
		Collections.sort(allChannels);

		List<String> channels = MapUtils.get(groupChannels, group, () -> {
			return new CopyOnWriteArrayList<String>();
		});
		for (int i = 0; i < weight; i++) {
			channels.add(channel);
		}
		Collections.sort(channels);
		log.info("Registered channel: {}.{}, Proportion: {}/{}", group, channel, channels.size(), allChannels.size());
	}

	public boolean hasRegistered(String group, String channel) {
		return groupChannels.containsKey(group) ? groupChannels.get(group).contains(channel) : false;
	}

	public void removeChannel(String group, String channel) {
		Assert.hasNoText(channel, "Channel is required.");
		while (allChannels.contains(channel)) {
			allChannels.remove(channel);
		}

		if (groupChannels.containsKey(group)) {
			List<String> channels = groupChannels.get(group);
			while (channels.contains(channel)) {
				channels.remove(channel);
			}
			log.info("Removed channel: {}.{}, Proportion: {}/{}", group, channel, channels.size(), allChannels.size());
		}

	}

	public int countOfChannel() {
		return new HashSet<String>(allChannels).size();
	}

	public int countOfChannel(String group) {
		return groupChannels.containsKey(group) ? new HashSet<String>(groupChannels.get(group)).size() : 0;
	}

	public void unicast(String topic, Object message) {
		unicast(topic, message, -1);
	}

	public void unicast(String topic, Object message, int timeout) {
		Assert.hasNoText(topic, "Topic must be required");
		String channel = loadBalancer.select(message, allChannels);
		if (StringUtils.isNotBlank(channel)) {
			doSendMessage(channel, topic, message, timeout);
		}
	}

	public void unicast(String group, String topic, Object message) {
		unicast(group, topic, message, -1);
	}

	public void unicast(String group, String topic, Object message, int timeout) {
		Assert.hasNoText(topic, "Topic must be required");
		if (groupChannels.containsKey(group)) {
			String channel = loadBalancer.select(message, groupChannels.get(group));
			if (StringUtils.isNotBlank(channel)) {
				doSendMessage(channel, topic, message, timeout);
			}
		}
	}

	public void multicast(String topic, Object message) {
		multicast(topic, message, -1);
	}

	public void multicast(String topic, Object message, int timeout) {
		Assert.hasNoText(topic, "Topic must be required");
		for (String channel : new HashSet<String>(allChannels)) {
			doSendMessage(channel, topic, message, timeout);
		}
	}

	public void multicast(String group, String topic, Object message) {
		multicast(group, topic, message, -1);
	}

	public void multicast(String group, String topic, Object message, int timeout) {
		Assert.hasNoText(topic, "Topic must be required");
		Set<String> copy = groupChannels.containsKey(group) ? new HashSet<String>(groupChannels.get(group)) : new HashSet<String>();
		for (String channel : copy) {
			doSendMessage(channel, topic, message, timeout);
		}
	}

	public void send(String channel, String topic, Object message) {
		send(channel, topic, message, -1);
	}

	public void send(String channel, String topic, Object message, int timeout) {
		Assert.hasNoText(channel, "Channel must be required");
		Assert.hasNoText(topic, "Topic must be required");
		doSendMessage(channel, topic, message, timeout);
	}

	public void ack(String channel, ClusterMulticastMessage messageObject) {
		messageObject.setTopic(MulticastMessageAcker.TOPIC_NAME);
		doSendMessage(channel, messageObject, -1);
	}

	private void doSendMessage(String channel, String topic, Object message, int timeout) {
		ClusterMulticastMessage messageObject = createMessageObject(channel, topic, message, timeout);
		doSendMessage(channel, messageObject, timeout);
	}

	void doSendMessage(String channel, ClusterMulticastMessage messageObject, int timeout) {
		redisMessageSender.sendMessage(channel, messageObject);
		if (timeout > 0) {
			clusterMulticastMessageAcker.waitForAck(messageObject);
		}
	}

	protected ClusterMulticastMessage createMessageObject(String channel, String topic, Object message, int timeout) {
		ClusterMulticastMessage messageObject = new ClusterMulticastMessage();
		messageObject.setApplicationInfo(instanceId.getApplicationInfo());
		messageObject.setChannel(channel);
		messageObject.setTopic(topic);
		messageObject.setMessage(message);
		messageObject.setTimeout(timeout);
		return messageObject;
	}

	private class ClusterMulticastMessageAckerChecker implements Executable {

		private Timer timer;

		public void start() {
			timer = ThreadUtils.scheduleWithFixedDelay(this, 5, 5, TimeUnit.SECONDS);
		}

		@Override
		public boolean execute() {
			clusterMulticastMessageAcker.retrySendMessage(ClusterMulticastGroup.this);
			return true;
		}

		public void close() {
			if (timer != null) {
				timer.cancel();
			}
		}

	}

	@Getter
	@Setter
	public static class ClusterMulticastMessage implements Serializable {

		private static final long serialVersionUID = 1L;

		public ClusterMulticastMessage() {
			this.id = UUID.randomUUID().toString();
			this.timestamp = System.currentTimeMillis();
		}

		private String id;
		private long timestamp;
		private ApplicationInfo applicationInfo;
		private String channel;
		private String topic;
		private Object message;
		private int timeout;

		@Override
		public int hashCode() {
			final int prime = 37;
			return prime + prime * id.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == this) {
				return true;
			}
			if (obj instanceof ClusterMulticastMessage) {
				ClusterMulticastMessage message = (ClusterMulticastMessage) obj;
				return message.getId().equals(getId());
			}
			return false;
		}

	}

	@PreDestroy
	public void close() {
		clusterMulticastMessageAckerChecker.close();
	}

}
