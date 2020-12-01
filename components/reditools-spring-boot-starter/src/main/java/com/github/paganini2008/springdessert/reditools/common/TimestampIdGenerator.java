package com.github.paganini2008.springdessert.reditools.common;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.support.atomic.RedisAtomicLong;

import com.github.paganini2008.devtools.collection.LruMap;
import com.github.paganini2008.devtools.collection.MapUtils;
import com.github.paganini2008.devtools.date.DateUtils;

/**
 * 
 * TimestampIdGenerator
 *
 * @author Fred Feng
 * @since 1.0
 */
public class TimestampIdGenerator implements IdGenerator {

	private static final String defaultDatePattern = "yyyyMMddHHmmss";
	private static final int maxConcurrency = 100000;
	private final String keyPrefix;
	private final RedisConnectionFactory connectionFactory;
	private final Map<String, RedisAtomicLong> cache;

	public TimestampIdGenerator(RedisConnectionFactory connectionFactory) {
		this("id:", connectionFactory);
	}

	public TimestampIdGenerator(String keyPrefix, RedisConnectionFactory connectionFactory) {
		this.keyPrefix = keyPrefix;
		this.connectionFactory = connectionFactory;
		this.cache = new LruMap<String, RedisAtomicLong>(60);
	}

	@Override
	public long generateId() {
		final String timestamp = DateUtils.format(System.currentTimeMillis(), defaultDatePattern);
		RedisAtomicLong counter = MapUtils.get(cache, timestamp, () -> {
			RedisAtomicLong l = new RedisAtomicLong(keyPrefix + timestamp, connectionFactory);
			l.expire(60, TimeUnit.SECONDS);
			return l;
		});
		return Long.parseLong(timestamp) * maxConcurrency + counter.incrementAndGet();
	}

}
