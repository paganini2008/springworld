package com.github.paganini2008.springdessert.webcrawler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.support.atomic.RedisAtomicInteger;

import com.github.paganini2008.devtools.Assert;
import com.github.paganini2008.devtools.RandomUtils;
import com.github.paganini2008.devtools.collection.MapUtils;
import com.github.paganini2008.xtransport.Tuple;

/**
 * 
 * CountingConditionalCompletion
 *
 * @author Fred Feng
 * 
 * @since 1.0
 */
public class CountingConditionalCompletion extends TimingConditionalCompletion {

	public CountingConditionalCompletion(String keyPrefix, RedisConnectionFactory redisConnectionFactory, long delay, TimeUnit timeUnit,
			int maxFetchSize) {
		super(keyPrefix, redisConnectionFactory, delay, timeUnit);
		Assert.lt(maxFetchSize, 100, "Minimun maxFetchSize is 100");
		this.maxFetchSize = maxFetchSize;
		this.keyPrefix = keyPrefix + "counting:";
		this.countableMap = new ConcurrentHashMap<Long, RedisAtomicInteger>();
	}

	private final int maxFetchSize;
	private final String keyPrefix;
	private final Map<Long, RedisAtomicInteger> countableMap;

	@Override
	protected boolean evaluate(Tuple tuple) {
		final Long catalogId = (Long) tuple.getField("catalogId");
		RedisAtomicInteger counter = MapUtils.get(countableMap, catalogId, () -> {
			return getInteger(catalogId);
		});
		return counter.incrementAndGet() > maxFetchSize;

	}

	private RedisAtomicInteger getInteger(long catalogId) {
		final String redisCounter = keyPrefix + catalogId;
		RedisAtomicInteger integer = new RedisAtomicInteger(redisCounter, redisConnectionFactory);
		integer.expire(getRemaining() + RandomUtils.randomLong(100, 1000), TimeUnit.MILLISECONDS);
		return integer;
	}

}
