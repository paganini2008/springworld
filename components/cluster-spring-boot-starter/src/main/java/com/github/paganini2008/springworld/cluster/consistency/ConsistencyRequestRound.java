package com.github.paganini2008.springworld.cluster.consistency;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.support.atomic.RedisAtomicLong;

import com.github.paganini2008.springworld.cluster.ContextClusterAware;

/**
 * 
 * ConsistencyRequestRound
 *
 * @author Fred Feng
 * @since 1.0
 */
public class ConsistencyRequestRound {

	private static final String CONSISTENCY_ROUND_PATTERN = "%s:consistency:round:%s";
	private final Map<String, RedisAtomicLong> serials = new ConcurrentHashMap<String, RedisAtomicLong>();

	@Value("${spring.application.name}")
	private String applicationName;

	@Autowired
	private RedisConnectionFactory connectionFactory;

	public long nextRound(String name) {
		final String redisCounter = String.format(CONSISTENCY_ROUND_PATTERN, ContextClusterAware.SPRING_CLUSTER_NAMESPACE + applicationName,
				name);
		return serials.getOrDefault(name, new RedisAtomicLong(redisCounter, connectionFactory)).incrementAndGet();
	}

	public long currentRound(String name) {
		final String redisCounter = String.format(CONSISTENCY_ROUND_PATTERN, ContextClusterAware.SPRING_CLUSTER_NAMESPACE + applicationName,
				name);
		return serials.getOrDefault(name, new RedisAtomicLong(redisCounter, connectionFactory)).get();
	}
	
}
