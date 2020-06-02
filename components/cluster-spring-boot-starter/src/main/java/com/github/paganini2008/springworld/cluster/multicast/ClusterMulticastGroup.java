package com.github.paganini2008.springworld.cluster.multicast;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.beans.factory.annotation.Autowired;

import com.github.paganini2008.devtools.Assert;
import com.github.paganini2008.devtools.StringUtils;
import com.github.paganini2008.springworld.cluster.ApplicationInfo;
import com.github.paganini2008.springworld.cluster.InstanceId;
import com.github.paganini2008.springworld.redisplus.messager.RedisMessageSender;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 * ClusterMulticastGroup
 *
 * @author Fred Feng
 * @version 1.0
 */
public class ClusterMulticastGroup {

	private final List<String> channels = new CopyOnWriteArrayList<String>();

	@Autowired
	private RedisMessageSender redisMessageSender;

	@Autowired
	private LoadBalance loadBalance;

	@Autowired
	private InstanceId instanceId;

	public void registerChannel(String channel, int weight) {
		Assert.hasNoText("Channel is required.");
		for (int i = 0; i < weight; i++) {
			channels.add(channel);
		}
		Collections.sort(channels);
	}

	public boolean hasRegistered(String channel) {
		return channels.contains(channel);
	}

	public void removeChannel(String channel) {
		Assert.hasNoText("Channel is required.");
		while (channels.contains(channel)) {
			channels.remove(channel);
		}
	}

	public int countOfChannel() {
		return channels.size();
	}

	public void unicast(Object message) {
		unicast("*", message);
	}

	public void unicast(String topic, Object message) {
		Assert.hasNoText("Topic is required");
		String channel = loadBalance.select(message, channels);
		if (StringUtils.isNotBlank(channel)) {
			redisMessageSender.sendMessage(channel, createObjectMessage(topic, message));
		}
		System.out.println(">>>>>> 发送：" + topic + ", message: " + message);
	}

	public void send(String channel, String topic, Object message) {
		Assert.hasNoText("Channel is required");
		Assert.hasNoText("Topic is required");
		redisMessageSender.sendMessage(channel, createObjectMessage(topic, message));
	}

	public void multicast(Object message) {
		multicast("*", message);
	}

	public void multicast(String topic, Object message) {
		Assert.hasNoText("Topic is required");
		for (String channel : new HashSet<String>(channels)) {
			redisMessageSender.sendMessage(channel, createObjectMessage(topic, message));
		}
	}

	protected ClusterMulticastMessage createObjectMessage(String topic, Object message) {
		ClusterMulticastMessage data = new ClusterMulticastMessage();
		data.setApplicationInfo(instanceId.getApplicationInfo());
		data.setTopic(topic);
		data.setMessage(message);
		return data;
	}

	@Getter
	@Setter
	public static class ClusterMulticastMessage implements Serializable {

		private static final long serialVersionUID = 1L;

		private ApplicationInfo applicationInfo;
		private String topic;
		private Object message;

	}

}
