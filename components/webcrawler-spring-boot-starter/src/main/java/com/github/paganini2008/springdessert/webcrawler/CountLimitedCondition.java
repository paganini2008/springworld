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
public class CountLimitedCondition implements FinishableCondition {

	private final String keyPrefix;
	private final int maxFetchSize;
	private final RedisConnectionFactory redisConnectionFactory;
	private final Map<Long, RedisAtomicInteger> counterMap;
	private final AtomicBoolean finished;

	public CountLimitedCondition(String keyPrefix, RedisConnectionFactory redisConnectionFactory, int maxFetchSize) {
		Assert.lt(maxFetchSize, 100, "Minimun maxFetchSize is 100");
		this.keyPrefix = keyPrefix;
		this.maxFetchSize = maxFetchSize;
		this.redisConnectionFactory = redisConnectionFactory;
		this.counterMap = new ConcurrentHashMap<Long, RedisAtomicInteger>();
		this.finished = new AtomicBoolean(false);
	}

	@Override
	public boolean mightFinish(Tuple tuple) {
		Long catalogId = (Long) tuple.getField("catalogId");
		String redisCounter = keyPrefix + catalogId;
		RedisAtomicInteger counter = MapUtils.get(counterMap, catalogId, () -> {
			return new RedisAtomicInteger(redisCounter, redisConnectionFactory);
		});
		if (finished.getAndSet(counter.incrementAndGet() > maxFetchSize)) {
			if (counterMap.containsKey(catalogId)) {
				log.info("Finish crawling work. Crawl {} records this time", counter.get());
				counter.expire(60, TimeUnit.SECONDS);
				counterMap.remove(catalogId);
			}
		}
		return isFinished();
	}

	@Override
	public boolean isFinished() {
		return finished.get();
	}

}
