package com.github.paganini2008.springworld.cluster.pool;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import com.github.paganini2008.springworld.cluster.ApplicationClusterLeaderStandbyEvent;
import com.github.paganini2008.springworld.redis.concurrents.Lifespan;
import com.github.paganini2008.springworld.redis.concurrents.RedisSharedLatch;
import com.github.paganini2008.springworld.redis.concurrents.SharedLatch;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * ContextClusterLatch
 *
 * @author Fred Feng
 * 
 * 
 * @version 1.0
 */
@Slf4j
public class ContextClusterLatch extends RedisSharedLatch implements SharedLatch, ApplicationListener<ApplicationClusterLeaderStandbyEvent> {

	private static final int DEFAULT_SHARED_LATCH_EXPIRATION = 60;

	public ContextClusterLatch(String name, int maxPermits, RedisConnectionFactory redisConnectionFactory) {
		super(name, maxPermits, redisConnectionFactory, DEFAULT_SHARED_LATCH_EXPIRATION);
	}

	@Autowired
	private Lifespan lifespan;

	@Override
	public void onApplicationEvent(ApplicationClusterLeaderStandbyEvent event) {
		keepAlive(lifespan, 3);
		log.info("ContextClusterLatch start to work.");
	}

}
