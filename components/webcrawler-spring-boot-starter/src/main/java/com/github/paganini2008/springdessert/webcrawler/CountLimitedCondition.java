package com.github.paganini2008.springdessert.webcrawler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.support.atomic.RedisAtomicInteger;

import com.github.paganini2008.devtools.Assert;
import com.github.paganini2008.devtools.collection.MapUtils;
import com.github.paganini2008.xtransport.Tuple;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * CountLimitedCondition
 *
 * @author Fred Feng
 * @since 1.0
 */
@Slf4j
public class CountLimitedCondition extends AbstractFinishableCondition {

	private final String keyPrefix;
	private final int maxFetchSize;
	private final RedisConnectionFactory redisConnectionFactory;
	private final Map<Long, RedisAtomicInteger> countableMap;

	public CountLimitedCondition(String keyPrefix, RedisConnectionFactory redisConnectionFactory, int maxFetchSize) {
		Assert.lt(maxFetchSize, 100, "Minimun maxFetchSize is 100");
		this.keyPrefix = keyPrefix;
		this.maxFetchSize = maxFetchSize;
		this.redisConnectionFactory = redisConnectionFactory;
		this.countableMap = new ConcurrentHashMap<Long, RedisAtomicInteger>();
	}

	@Override
	public boolean mightFinish(Tuple tuple) {
		if (isFinished(tuple)) {
			return true;
		}
		final Long catalogId = (Long) tuple.getField("catalogId");
		String redisCounter = keyPrefix + catalogId;
		RedisAtomicInteger counter = MapUtils.get(countableMap, catalogId, () -> {
			return new RedisAtomicInteger(redisCounter, redisConnectionFactory);
		});

		boolean finished = counter.incrementAndGet() > maxFetchSize;
		MapUtils.get(finishableMap, catalogId, () -> {
			return new AtomicBoolean(false);
		}).set(finished);

		if (finished) {
			counter = countableMap.remove(catalogId);
			if (counter != null) {
				log.info("Finish crawling work. Crawl {} records this time", counter.get());
				counter.expire(1, TimeUnit.SECONDS);
			}
		}
		return isFinished(tuple);
	}

}
