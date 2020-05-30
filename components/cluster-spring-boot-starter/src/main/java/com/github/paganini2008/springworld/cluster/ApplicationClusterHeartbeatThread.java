package com.github.paganini2008.springworld.cluster;

import java.util.Timer;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;

import com.github.paganini2008.devtools.multithreads.Executable;
import com.github.paganini2008.devtools.multithreads.ThreadUtils;
import com.github.paganini2008.springworld.redis.BeanNames;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * Start a backend thread, which constantly prolong ttl, to keep heartbeating
 * between application and redis.
 * 
 * @author Fred Feng
 * @version 1.0
 */
@Slf4j
public class ApplicationClusterHeartbeatThread implements Executable {

	private static final int MIN_LIFESPAN_TTL = 5;
	private static final int MAX_LIFESPAN_TTL = 15;

	@Value("${spring.application.cluster.lifespanTtl:5}")
	private int lifespanTtl;

	@Value("${spring.application.name}")
	private String applicationName;

	@Qualifier(BeanNames.REDIS_TEMPLATE)
	@Autowired
	private RedisTemplate<String, Object> redisTemplate;

	@Override
	public boolean execute() {
		final String key = ApplicationClusterAware.APPLICATION_CLUSTER_NAMESPACE + applicationName;
		redisTemplate.expire(key, lifespanTtl, TimeUnit.SECONDS);
		return true;
	}

	private Timer timer;

	public void start() {
		if (lifespanTtl < MIN_LIFESPAN_TTL || lifespanTtl > MAX_LIFESPAN_TTL) {
			throw new IllegalArgumentException("The value range of parameter 'spring.application.cluster.lifespanTtl' is between "
					+ MIN_LIFESPAN_TTL + " and " + MAX_LIFESPAN_TTL);
		}
		final String key = ApplicationClusterAware.APPLICATION_CLUSTER_NAMESPACE + applicationName;
		redisTemplate.expire(key, lifespanTtl, TimeUnit.SECONDS);
		timer = ThreadUtils.scheduleAtFixedRate(this, 3, 3, TimeUnit.SECONDS);
		log.info("Start ApplicationClusterHeartbeatThread ok.");
	}

	public void stop() {
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
	}

}
