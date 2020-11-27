package com.github.paganini2008.springdessert.webcrawler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.support.atomic.RedisAtomicInteger;

import com.github.paganini2008.devtools.collection.MapUtils;
import com.github.paganini2008.transport.Tuple;

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

	public CountLimitedCondition(String keyPrefix, RedisConnectionFactory redisConnectionFactory, int maxFetchSize) {
		this.keyPrefix = keyPrefix;
		this.maxFetchSize = maxFetchSize;
		this.redisConnectionFactory = redisConnectionFactory;
		this.counterMap = new ConcurrentHashMap<Long, RedisAtomicInteger>();
	}

	@Override
	public boolean couldFinish(Tuple tuple) {
		Long catalogId = (Long) tuple.getField("catalogId");
		String redisCounter = keyPrefix + catalogId;
		RedisAtomicInteger counter = MapUtils.get(counterMap, catalogId, () -> {
			return new RedisAtomicInteger(redisCounter, redisConnectionFactory);
		});
		boolean finish;
		if (finish = counter.incrementAndGet() > maxFetchSize) {
			log.info("Finish crawling work. Crawl {} records this time", counter.get());
			counter.expire(60, TimeUnit.SECONDS);
			counterMap.remove(catalogId);
		}
		return finish;
	}

}
