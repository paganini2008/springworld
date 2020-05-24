package com.github.paganini2008.springworld.cluster.multicast;

import java.util.Timer;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.github.paganini2008.devtools.multithreads.Executable;
import com.github.paganini2008.devtools.multithreads.ThreadUtils;
import com.github.paganini2008.springworld.cluster.ApplicationClusterAware;
import com.github.paganini2008.springworld.cluster.InstanceId;
import com.github.paganini2008.springworld.redis.pubsub.RedisMessageSender;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * ClusterMulticastHeartbeatThread
 * 
 * @author Fred Feng
 * @version 1.0
 */
@Slf4j
public class ClusterMulticastHeartbeatThread implements Executable {

	private static final int MIN_LIFESPAN_TTL = 5;
	private static final int MAX_LIFESPAN_TTL = 15;

	@Value("${spring.application.cluster.member.lifespanTtl:5}")
	private int lifespanTtl;

	@Value("${spring.application.name}")
	private String applicationName;

	@Autowired
	private RedisMessageSender redisMessageSender;

	@Autowired
	private InstanceId clusterId;

	private Timer timer;

	private String channel;

	@Override
	public boolean execute() {
		redisMessageSender.sendEphemeralMessage(channel, clusterId.get(), lifespanTtl, TimeUnit.SECONDS);
		return true;
	}

	public void start() {
		if (lifespanTtl < MIN_LIFESPAN_TTL || lifespanTtl > MAX_LIFESPAN_TTL) {
			throw new IllegalArgumentException("The value range of parameter 'spring.application.cluster.lifespanTtl' is between "
					+ MIN_LIFESPAN_TTL + " and " + MAX_LIFESPAN_TTL);
		}
		this.channel = ApplicationClusterAware.APPLICATION_CLUSTER_NAMESPACE + applicationName + ":" + clusterId.get();
		redisMessageSender.sendEphemeralMessage(channel, clusterId.get(), lifespanTtl, TimeUnit.SECONDS);
		timer = ThreadUtils.scheduleAtFixedRate(this, 3, 3, TimeUnit.SECONDS);
		log.info("Start ClusterMulticastHeartbeatThread ok.");
	}

	public void stop() {
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
	}
}