package com.github.paganini2008.springdessert.reditools.common;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.support.atomic.RedisAtomicLong;

import com.github.paganini2008.devtools.collection.MapUtils;
import com.github.paganini2008.devtools.date.DateUtils;
import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;
import com.googlecode.concurrentlinkedhashmap.EvictionListener;
import com.googlecode.concurrentlinkedhashmap.Weighers;

/**
 * 
 * TimestampIdGenerator
 *
 * @author Fred Feng
 * @since 1.0
 */
public class TimestampIdGenerator implements IdGenerator, EvictionListener<String, RedisAtomicLong> {

	private static final String defaultDatePattern = "yyyyMMddHHmmss";
	private static final int maxConcurrency = 100000;
	private final Map<String, RedisAtomicLong> cache;
	private final RedisConnectionFactory connectionFactory;

	public TimestampIdGenerator(RedisConnectionFactory connectionFactory) {
		this.connectionFactory = connectionFactory;
		this.cache = new ConcurrentLinkedHashMap.Builder<String, RedisAtomicLong>().maximumWeightedCapacity(16)
				.weigher(Weighers.singleton()).listener(this).build();
	}

	@Override
	public long generateId() {
		final String key = DateUtils.format(System.currentTimeMillis(), defaultDatePattern);
		RedisAtomicLong counter = MapUtils.get(cache, key, () -> {
			RedisAtomicLong l = new RedisAtomicLong("traceId:" + key, connectionFactory);
			l.expire(60, TimeUnit.SECONDS);
			return l;
		});
		return Long.parseLong(key) * maxConcurrency + counter.getAndIncrement();
	}

	@Override
	public void onEviction(String key, RedisAtomicLong value) {
		value.expire(1, TimeUnit.SECONDS);
	}

}
