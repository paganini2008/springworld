package com.github.paganini2008.springdessert.webcrawler;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;

import com.github.paganini2008.devtools.collection.MapUtils;
import com.github.paganini2008.devtools.date.DateUtils;
import com.github.paganini2008.xtransport.Tuple;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * TimeLimitedCondition
 *
 * @author Fred Feng
 * @since 1.0
 */
@Slf4j
public class TimeLimitedCondition extends AbstractFinishableCondition {

	private final String keyPrefix;
	private final long remaining;
	private final RedisConnectionFactory redisConnectionFactory;
	private final Map<Long, RedisTemplate<String, Long>> timestampMap;

	public TimeLimitedCondition(String keyPrefix, RedisConnectionFactory redisConnectionFactory, long delay, TimeUnit timeUnit) {
		this.keyPrefix = keyPrefix;
		this.redisConnectionFactory = redisConnectionFactory;
		this.remaining = DateUtils.convertToMillis(delay, timeUnit);
		this.timestampMap = new ConcurrentHashMap<Long, RedisTemplate<String, Long>>();
	}

	@Override
	public boolean mightFinish(Tuple tuple) {
		if (isFinished(tuple)) {
			return true;
		}
		final Long catalogId = (Long) tuple.getField("catalogId");
		String key = keyPrefix + catalogId;
		RedisTemplate<String, Long> redisTemplate = MapUtils.get(timestampMap, catalogId, () -> {
			RedisTemplate<String, Long> l = getLong();
			l.opsForValue().set(key, System.currentTimeMillis() + remaining);
			return l;
		});

		boolean finished = System.currentTimeMillis() > redisTemplate.opsForValue().get(key);
		MapUtils.get(finishableMap, catalogId, () -> {
			return new AtomicBoolean(false);
		}).set(finished);

		if (finished) {
			redisTemplate = timestampMap.remove(catalogId);
			if (redisTemplate != null) {
				long timestamp = redisTemplate.opsForValue().get(key);
				log.info("Finish crawling work on deadline: {}", new Date(timestamp));
				if (redisTemplate.hasKey(key)) {
					redisTemplate.delete(key);
				}
			}
		}
		return isFinished(tuple);
	}

	protected RedisTemplate<String, Long> getLong() {
		RedisTemplate<String, Long> redisTemplate = new RedisTemplate<>();
		redisTemplate.setKeySerializer(RedisSerializer.string());
		redisTemplate.setValueSerializer(new GenericToStringSerializer<>(Long.class));
		redisTemplate.setExposeConnection(true);
		redisTemplate.setConnectionFactory(redisConnectionFactory);
		redisTemplate.afterPropertiesSet();
		return redisTemplate;
	}

}
