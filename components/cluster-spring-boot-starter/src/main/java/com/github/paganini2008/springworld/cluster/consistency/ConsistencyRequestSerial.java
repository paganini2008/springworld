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
import com.github.paganini2008.springworld.cluster.InstanceId;

/**
 * 
 * ConsistencyRequestSerial
 *
 * @author Fred Feng
 * @since 1.0
 */
public class ConsistencyRequestSerial {

	private static final String CONSISTENCY_SERIAL_PATTERN = "%s:consistency:serial:%s";
	private final Map<String, RedisAtomicLong> serials = new ConcurrentHashMap<String, RedisAtomicLong>();

	@Value("${spring.application.name}")
	private String applicationName;

	@Autowired
	private InstanceId instanceId;

	@Autowired
	private RedisConnectionFactory connectionFactory;

	public long nextSerial(String name) {
		final String redisCounter = String.format(CONSISTENCY_SERIAL_PATTERN,
				ApplicationClusterAware.APPLICATION_CLUSTER_NAMESPACE + applicationName, name);
		try {
			RedisAtomicLong counter = MapUtils.get(serials, name, () -> {
				return new RedisAtomicLong(redisCounter, connectionFactory);
			});
			return instanceId.getWeight() > 1 ? counter.incrementAndGet() + instanceId.getWeight() : counter.incrementAndGet();
		} catch (Exception e) {
			serials.remove(name);
			return nextSerial(name);
		}
	}

	public long currentSerial(String name) {
		final String redisCounter = String.format(CONSISTENCY_SERIAL_PATTERN,
				ApplicationClusterAware.APPLICATION_CLUSTER_NAMESPACE + applicationName, name);
		try {
			return MapUtils.get(serials, name, () -> {
				return new RedisAtomicLong(redisCounter, connectionFactory);
			}).get();
		} catch (Exception e) {
			serials.remove(name);
			return currentSerial(name);
		}
	}

	public void clean(String name) {
		RedisAtomicLong counter = serials.remove(name);
		if (counter != null) {
			try {
				counter.expire(ConsistencyRequestContext.CONSISTENCY_REQUEST_MAX_TIMEOUT, TimeUnit.SECONDS);
			} catch (Exception ignored) {
			}
		}
	}

}
