package com.github.paganini2008.springworld.cluster.consistency;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.support.atomic.RedisAtomicLong;

import com.github.paganini2008.devtools.collection.MapUtils;
import com.github.paganini2008.springworld.cluster.ApplicationClusterAware;

/**
 * 
 * ConsistencyRequestRound
 *
 * @author Fred Feng
 * @since 1.0
 */
public class ConsistencyRequestRound {

	private static final String CONSISTENCY_ROUND_PATTERN = "%s:consistency:round:%s";
	private final Map<String, RedisAtomicLong> rounds = new ConcurrentHashMap<String, RedisAtomicLong>();

	@Value("${spring.application.name}")
	private String applicationName;

	@Autowired
	private RedisConnectionFactory connectionFactory;

	public long nextRound(String name) {
		final String redisCounter = String.format(CONSISTENCY_ROUND_PATTERN,
				ApplicationClusterAware.APPLICATION_CLUSTER_NAMESPACE + applicationName, name);
		try {
			return MapUtils.get(rounds, name, () -> {
				return new RedisAtomicLong(redisCounter, connectionFactory);
			}).incrementAndGet();
		} catch (Exception e) {
			rounds.remove(name);
			return nextRound(name);
		}
	}

	public long currentRound(String name) {
		final String redisCounter = String.format(CONSISTENCY_ROUND_PATTERN,
				ApplicationClusterAware.APPLICATION_CLUSTER_NAMESPACE + applicationName, name);
		try {
			return MapUtils.get(rounds, name, () -> {
				return new RedisAtomicLong(redisCounter, connectionFactory);
			}).get();
		} catch (Exception e) {
			rounds.remove(name);
			return currentRound(name);
		}
	}

	public void clean(String name) {
		RedisAtomicLong counter = rounds.remove(name);
		if (counter != null) {
			try {
				counter.expire(ConsistencyRequestContext.CONSISTENCY_REQUEST_MAX_TIMEOUT, TimeUnit.SECONDS);
			} catch (Exception ignored) {
			}
		}
	}

}
