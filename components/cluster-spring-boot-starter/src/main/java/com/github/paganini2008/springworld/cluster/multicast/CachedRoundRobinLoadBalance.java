package com.github.paganini2008.springworld.cluster.multicast;

import java.util.List;

import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.support.atomic.RedisAtomicLong;

import com.github.paganini2008.devtools.collection.CollectionUtils;

/**
 * 
 * CachedRoundRobinLoadBalance
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class CachedRoundRobinLoadBalance implements LoadBalance {

	private final RedisAtomicLong counter;

	public CachedRoundRobinLoadBalance(String key, RedisConnectionFactory connectionFactory) {
		this.counter = new RedisAtomicLong(key, connectionFactory);
	}

	@Override
	public String select(Object message, List<String> channels) {
		if (CollectionUtils.isEmpty(channels)) {
			return null;
		}
		return channels.get((int) (counter.getAndIncrement() % channels.size()));
	}

}
