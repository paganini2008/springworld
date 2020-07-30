package com.github.paganini2008.springworld.cluster.multicast;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.beans.factory.annotation.Autowired;

import com.github.paganini2008.devtools.Assert;
import com.github.paganini2008.devtools.StringUtils;
import com.github.paganini2008.devtools.collection.MapUtils;
import com.github.paganini2008.springworld.cluster.ApplicationInfo;
import com.github.paganini2008.springworld.cluster.InstanceId;
import com.github.paganini2008.springworld.redisplus.messager.RedisMessageSender;

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

	@Autowired
	private LoadBalance loadBalance;

	@Autowired
	private InstanceId instanceId;

	public void registerChannel(String group, String channel, int weight) {
		Assert.hasNoText(channel, "Channel must be required.");
		Assert.lt(weight, 0, "Weight must be > 0");

		for (int i = 0; i < weight; i++) {
			allChannels.add(channel);
		}
		Collections.sort(allChannels);
		if (log.isTraceEnabled()) {
			log.trace("Current channels'size: {}", allChannels.size());
		}

		List<String> channels = MapUtils.get(groupChannels, group, () -> {
			return new CopyOnWriteArrayList<String>();
		});
		for (int i = 0; i < weight; i++) {
			channels.add(channel);
		}
		Collections.sort(channels);
		if (log.isTraceEnabled()) {
			log.trace("Current channels'size of {}: {}", group, channels.size());
		}
	}

	public boolean hasRegistered(String group, String channel) {
		return groupChannels.containsKey(group) ? groupChannels.get(group).contains(channel) : false;
	}

	public void removeChannel(String group, String channel) {
		Assert.hasNoText(channel, "Channel is required.");
		while (allChannels.contains(channel)) {
			allChannels.remove(channel);
		}
		if (log.isTraceEnabled()) {
			log.trace("Current channels'size: {}", allChannels.size());
		}

		if (groupChannels.containsKey(group)) {
			List<String> channels = groupChannels.get(group);
			while (channels.contains(channel)) {
				channels.remove(channel);
			}
			if (log.isTraceEnabled()) {
				log.trace("Current channels'size of {}: {}", group, channels.size());
			}
		}
	}

	public int countOfChannel() {
		return new HashSet<String>(allChannels).size();
	}

	public int countOfChannel(String group) {
		return groupChannels.containsKey(group) ? new HashSet<String>(groupChannels.get(group)).size() : 0;
	}

	public void unicast(String topic, Object message) {
		Assert.hasNoText(topic, "Topic must be required");
		String channel = loadBalance.select(message, allChannels);
		if (StringUtils.isNotBlank(channel)) {
			redisMessageSender.sendMessage(channel, createObjectMessage(topic, message));
		}
	}

	public void unicast(String group, String topic, Object message) {
		Assert.hasNoText(topic, "Topic must be required");
		String channel = loadBalance.select(message, groupChannels.get(group));
		if (StringUtils.isNotBlank(channel)) {
			redisMessageSender.sendMessage(channel, createObjectMessage(topic, message));
		}
	}

	public void multicast(String topic, Object message) {
		Assert.hasNoText(topic, "Topic must be required");
		for (String channel : new HashSet<String>(allChannels)) {
			redisMessageSender.sendMessage(channel, createObjectMessage(topic, message));
		}
	}

	public void multicast(String group, String topic, Object message) {
		Assert.hasNoText(topic, "Topic must be required");
		for (String channel : new HashSet<String>(groupChannels.get(group))) {
			redisMessageSender.sendMessage(channel, createObjectMessage(topic, message));
		}
	}

	public void send(String channel, String topic, Object message) {
		Assert.hasNoText(channel, "Channel must be required");
		Assert.hasNoText(topic, "Topic must be required");
		redisMessageSender.sendMessage(channel, createObjectMessage(topic, message));
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
