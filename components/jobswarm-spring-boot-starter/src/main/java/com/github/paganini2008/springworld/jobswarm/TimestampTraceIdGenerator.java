package com.github.paganini2008.springworld.jobswarm;

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
 * TimestampTraceIdGenerator
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class TimestampTraceIdGenerator implements TraceIdGenerator, EvictionListener<String, RedisAtomicLong> {

	private final Map<String, RedisAtomicLong> cache;
	private final RedisConnectionFactory connectionFactory;

	public TimestampTraceIdGenerator(RedisConnectionFactory connectionFactory) {
		this.connectionFactory = connectionFactory;
		this.cache = new ConcurrentLinkedHashMap.Builder<String, RedisAtomicLong>().maximumWeightedCapacity(16)
				.weigher(Weighers.singleton()).listener(this).build();
	}

	@Override
	public long generateTraceId(JobKey jobKey) {
		final String key = DateUtils.format(System.currentTimeMillis(), "yyyyMMddHHmmss");
		RedisAtomicLong counter = MapUtils.get(cache, key, () -> {
			RedisAtomicLong l = new RedisAtomicLong("traceId:" + key, connectionFactory);
			l.expire(60, TimeUnit.SECONDS);
			return l;
		});
		return Long.parseLong(key) * 100000 + counter.getAndIncrement();
	}

	@Override
	public void onEviction(String key, RedisAtomicLong value) {
		value.expire(1, TimeUnit.SECONDS);
	}

}
