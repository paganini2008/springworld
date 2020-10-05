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
import com.github.paganini2008.springworld.reditools.common.TtlKeeper;

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

	@Value("${spring.application.cluster.name:default}")
	private String clusterName;

	@Autowired
	private RedisConnectionFactory connectionFactory;
	
	@Autowired
	private TtlKeeper ttlKeeper;

	public long nextRound(String name) {
		final String redisCounterName = counterName(name);
		try {
			return MapUtils.get(rounds, name, () -> {
				RedisAtomicLong l = new RedisAtomicLong(redisCounterName, connectionFactory);
				ttlKeeper.keep(l.getKey(), 5, TimeUnit.SECONDS);
				return l;
			}).incrementAndGet();
		} catch (Exception e) {
			rounds.remove(name);
			return nextRound(name);
		}
	}

	public long currentRound(String name) {
		final String redisCounterName = counterName(name);
		try {
			return MapUtils.get(rounds, name, () -> {
				RedisAtomicLong l = new RedisAtomicLong(redisCounterName, connectionFactory);
				ttlKeeper.keep(l.getKey(), 5, TimeUnit.SECONDS);
				return l;
			}).get();
		} catch (Exception e) {
			rounds.remove(name);
			return currentRound(name);
		}
	}

	public void clean(String name) {
		rounds.remove(name);
	}

	private String counterName(String name) {
		return String.format(CONSISTENCY_ROUND_PATTERN, ApplicationClusterAware.APPLICATION_CLUSTER_NAMESPACE + clusterName, name);
	}

}
