package com.github.paganini2008.springdessert.cluster.multicast;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.github.paganini2008.devtools.Assert;
import com.github.paganini2008.devtools.collection.MapUtils;
import com.github.paganini2008.devtools.multithreads.Executable;
import com.github.paganini2008.devtools.multithreads.ThreadUtils;
import com.github.paganini2008.springdessert.cluster.ApplicationInfo;
import com.github.paganini2008.springdessert.cluster.InstanceId;
import com.github.paganini2008.springdessert.cluster.LoadBalancer;
import com.github.paganini2008.springdessert.reditools.messager.RedisMessageSender;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * ApplicationMulticastGroup
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
@Slf4j
public class ApplicationMulticastGroup {

	private final List<ApplicationInfo> allCandidates = new CopyOnWriteArrayList<ApplicationInfo>();
	private final Map<String, List<ApplicationInfo>> groupCandidates = new ConcurrentHashMap<String, List<ApplicationInfo>>();

	@Autowired
	private RedisMessageSender redisMessageSender;

	@Qualifier("applicationMulticastLoadBalancer")
	@Autowired
	private LoadBalancer loadBalancer;

	@Autowired
	private InstanceId instanceId;

	@Autowired
	private MulticastMessageAcker multicastMessageAcker;

	private MulticastMessageAckerChecker multicastMessageAckerChecker;

	@PostConstruct
	public void configure() {
		multicastMessageAckerChecker = new MulticastMessageAckerChecker();
		multicastMessageAckerChecker.start();
	}

	public void registerCandidate(ApplicationInfo applicationInfo) {
		for (int i = 0; i < applicationInfo.getWeight(); i++) {
			allCandidates.add(applicationInfo);
		}
		if (allCandidates.size() > 1) {
			Collections.sort(allCandidates);
		}

		List<ApplicationInfo> candidates = MapUtils.get(groupCandidates, applicationInfo.getApplicationName(), () -> {
			return new CopyOnWriteArrayList<ApplicationInfo>();
		});
		for (int i = 0; i < applicationInfo.getWeight(); i++) {
			candidates.add(applicationInfo);
		}
		if (candidates.size() > 1) {
			Collections.sort(candidates);
		}
		log.info("Registered candidate: {}, Proportion: {}/{}", applicationInfo, candidates.size(), allCandidates.size());
	}

	public boolean hasRegistered(ApplicationInfo applicationInfo) {
		return groupCandidates.containsKey(applicationInfo.getApplicationName())
				? groupCandidates.get(applicationInfo.getApplicationName()).contains(applicationInfo)
				: false;
	}

	public void removeCandidate(ApplicationInfo applicationInfo) {
		while (allCandidates.contains(applicationInfo)) {
			allCandidates.remove(applicationInfo);
		}

		if (groupCandidates.containsKey(applicationInfo.getApplicationName())) {
			List<ApplicationInfo> candidates = groupCandidates.get(applicationInfo.getApplicationName());
			while (candidates.contains(applicationInfo)) {
				candidates.remove(applicationInfo);
			}
			log.info("Removed candidate: {}, Proportion: {}/{}", applicationInfo, candidates.size(), allCandidates.size());
		}

	}

	public int countOfCandidate() {
		return getCandidates().length;
	}

	public int countOfCandidate(String group) {
		return getCandidates(group).length;
	}

	public ApplicationInfo[] getCandidates() {
		return new TreeSet<ApplicationInfo>(allCandidates).toArray(new ApplicationInfo[0]);
	}

	public ApplicationInfo[] getCandidates(String group) {
		if (groupCandidates.containsKey(group)) {
			return new TreeSet<ApplicationInfo>(groupCandidates.get(group)).toArray(new ApplicationInfo[0]);
		}
		return new ApplicationInfo[0];
	}

	public void unicast(String topic, Object message) {
		unicast(topic, message, -1);
	}

	public void unicast(String topic, Object message, int timeout) {
		Assert.hasNoText(topic, "Topic must be required");
		ApplicationInfo applicationInfo = loadBalancer.select(message, allCandidates);
		if (applicationInfo != null) {
			doSendMessage(applicationInfo.getId(), topic, message, timeout);
		}
	}

	public void unicast(String group, String topic, Object message) {
		unicast(group, topic, message, -1);
	}

	public void unicast(String group, String topic, Object message, int timeout) {
		Assert.hasNoText(topic, "Topic must be required");
		if (groupCandidates.containsKey(group)) {
			ApplicationInfo applicationInfo = loadBalancer.select(message, groupCandidates.get(group));
			if (applicationInfo != null) {
				doSendMessage(applicationInfo.getId(), topic, message, timeout);
			}
		}
	}

	public void multicast(String topic, Object message) {
		multicast(topic, message, -1);
	}

	public void multicast(String topic, Object message, int timeout) {
		Assert.hasNoText(topic, "Topic must be required");
		for (ApplicationInfo applicationInfo : getCandidates()) {
			doSendMessage(applicationInfo.getId(), topic, message, timeout);
		}
	}

	public void multicast(String group, String topic, Object message) {
		multicast(group, topic, message, -1);
	}

	public void multicast(String group, String topic, Object message, int timeout) {
		Assert.hasNoText(topic, "Topic must be required");
		for (ApplicationInfo applicationInfo : getCandidates(group)) {
			doSendMessage(applicationInfo.getId(), topic, message, timeout);
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

	public void ack(String channel, MulticastMessage messageObject) {
		messageObject.setTopic(MulticastMessageAcker.DEFAULT_TOPIC_NAME);
		doSendMessage(channel, messageObject, -1);
	}

	private void doSendMessage(String channel, String topic, Object message, int timeout) {
		MulticastMessage messageObject = createMessageObject(channel, topic, message, timeout);
		doSendMessage(channel, messageObject, timeout);
	}

	void doSendMessage(String channel, MulticastMessage messageObject, int timeout) {
		redisMessageSender.sendMessage(channel, messageObject);
		if (timeout > 0) {
			multicastMessageAcker.waitForAck(messageObject);
		}
	}

	protected MulticastMessage createMessageObject(String channel, String topic, Object message, int timeout) {
		MulticastMessage messageObject = new MulticastMessage();
		messageObject.setApplicationInfo(instanceId.getApplicationInfo());
		messageObject.setChannel(channel);
		messageObject.setTopic(topic);
		messageObject.setMessage(message);
		messageObject.setTimeout(timeout);
		return messageObject;
	}

	private class MulticastMessageAckerChecker implements Executable {

		private Timer timer;

		public void start() {
			timer = ThreadUtils.scheduleWithFixedDelay(this, 5, 5, TimeUnit.SECONDS);
		}

		@Override
		public boolean execute() {
			multicastMessageAcker.retrySendMessage(ApplicationMulticastGroup.this);
			return true;
		}

		public void close() {
			if (timer != null) {
				timer.cancel();
			}
		}

	}

	/**
	 * 
	 * MulticastMessage
	 * 
	 * @author Jimmy Hoff
	 *
	 * @since 1.0
	 */
	@Getter
	@Setter
	public static class MulticastMessage implements Serializable {

		private static final long serialVersionUID = 1L;

		public MulticastMessage() {
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
			if (obj instanceof MulticastMessage) {
				MulticastMessage message = (MulticastMessage) obj;
				return message.getId().equals(getId());
			}
			return false;
		}

	}

	@PreDestroy
	public void close() {
		multicastMessageAckerChecker.close();
	}

}
