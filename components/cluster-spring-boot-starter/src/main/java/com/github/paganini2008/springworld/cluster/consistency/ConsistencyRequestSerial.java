package com.github.paganini2008.springworld.cluster.consistency;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.support.atomic.RedisAtomicLong;

import com.github.paganini2008.devtools.collection.MapUtils;
import com.github.paganini2008.springdessert.reditools.common.TtlKeeper;
import com.github.paganini2008.springworld.cluster.ApplicationClusterAware;
import com.github.paganini2008.springworld.cluster.InstanceId;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * ConsistencyRequestSerial
 *
 * @author Fred Feng
 * @since 1.0
 */
@Slf4j
public class ConsistencyRequestSerial {

	private static final String CONSISTENCY_SERIAL_PATTERN = "%s:consistency:serial:%s";
	private final Map<String, RedisAtomicLong> serials = new ConcurrentHashMap<String, RedisAtomicLong>();

	@Value("${spring.application.cluster.name}")
	private String clusterName;

	@Autowired
	private InstanceId instanceId;

	@Autowired
	private RedisConnectionFactory connectionFactory;

	@Autowired
	private TtlKeeper ttlKeeper;

	public long nextSerial(String name) {
		final String redisCounterName = counterName(name);
		try {
			RedisAtomicLong counter = MapUtils.get(serials, name, () -> {
				RedisAtomicLong l = new RedisAtomicLong(redisCounterName, connectionFactory);
				ttlKeeper.keepAlive(l.getKey(), 5);
				return l;
			});
			return instanceId.getWeight() > 1 ? counter.incrementAndGet() + instanceId.getWeight() : counter.incrementAndGet();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			serials.remove(name);
			return nextSerial(name);
		}
	}

	public long currentSerial(String name) {
		final String redisCounterName = counterName(name);
		try {
			return MapUtils.get(serials, name, () -> {
				RedisAtomicLong l = new RedisAtomicLong(redisCounterName, connectionFactory);
				ttlKeeper.keepAlive(l.getKey(), 5);
				return l;
			}).get();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			serials.remove(name);
			return currentSerial(name);
		}
	}

	public void clean(String name) {
		serials.remove(name);
	}

	private String counterName(String name) {
		return String.format(CONSISTENCY_SERIAL_PATTERN, ApplicationClusterAware.APPLICATION_CLUSTER_NAMESPACE + clusterName, name);
	}

}
