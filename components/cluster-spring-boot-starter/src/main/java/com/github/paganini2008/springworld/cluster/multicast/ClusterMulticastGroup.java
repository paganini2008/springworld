package com.github.paganini2008.springworld.cluster.multicast;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.beans.factory.annotation.Autowired;

import com.github.paganini2008.devtools.Assert;
import com.github.paganini2008.devtools.StringUtils;
import com.github.paganini2008.springworld.cluster.InstanceId;
import com.github.paganini2008.springworld.redis.pubsub.RedisMessageSender;

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
			redisMessageSender.sendMessage(channel, createObjectMessage(instanceId.get(), topic, message));
		}
	}

	public void send(String channel, String topic, Object message) {
		Assert.hasNoText("Channel is required");
		Assert.hasNoText("Topic is required");
		redisMessageSender.sendMessage(channel, createObjectMessage(instanceId.get(), topic, message));
	}

	public void multicast(Object message) {
		multicast("*", message);
	}

	public void multicast(String topic, Object message) {
		Assert.hasNoText("Topic is required");
		for (String channel : new HashSet<String>(channels)) {
			redisMessageSender.sendMessage(channel, createObjectMessage(instanceId.get(), topic, message));
		}
	}

	protected Object createObjectMessage(String instanceId, String topic, Object message) {
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("instanceId", instanceId);
		data.put("topic", topic);
		data.put("message", message);
		data.put("timestamp", System.currentTimeMillis());
		return data;
	}

}
