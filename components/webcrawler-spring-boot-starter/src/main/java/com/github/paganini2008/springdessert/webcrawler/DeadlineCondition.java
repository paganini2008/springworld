package com.github.paganini2008.springdessert.webcrawler;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;

import com.github.paganini2008.devtools.collection.MapUtils;
import com.github.paganini2008.transport.Tuple;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * DeadlineCondition
 *
 * @author Fred Feng
 * @since 1.0
 */
@Slf4j
public class DeadlineCondition implements FinishCondition {

	private final String keyPrefix;
	private final Date deadline;
	private final RedisConnectionFactory redisConnectionFactory;
	private final Map<Long, RedisTemplate<String, Long>> timestampMap;

	public DeadlineCondition(String keyPrefix, RedisConnectionFactory redisConnectionFactory, Date deadline) {
		this.keyPrefix = keyPrefix;
		this.redisConnectionFactory = redisConnectionFactory;
		this.deadline = deadline;
		this.timestampMap = new ConcurrentHashMap<Long, RedisTemplate<String, Long>>();
	}

	@Override
	public boolean couldFinish(Tuple tuple) {
		Long catalogId = (Long) tuple.getField("catalogId");
		String key = keyPrefix + catalogId;
		RedisTemplate<String, Long> redisTemplate = MapUtils.get(timestampMap, catalogId, () -> {
			return getLong();
		});
		redisTemplate.opsForValue().set(key, System.currentTimeMillis());
		boolean finish;
		if (finish = redisTemplate.opsForValue().get(key) > deadline.getTime()) {
			long timestamp = redisTemplate.opsForValue().get(key);
			log.info("Finish crawling work on deadline: {}", new Date(timestamp));
			redisTemplate.expire(key, 1, TimeUnit.SECONDS);
			timestampMap.remove(catalogId);
		}
		return finish;
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
