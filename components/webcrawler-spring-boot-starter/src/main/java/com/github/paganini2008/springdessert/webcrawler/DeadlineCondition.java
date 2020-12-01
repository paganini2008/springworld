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
 * DeadlineCondition
 *
 * @author Fred Feng
 * @since 1.0
 */
@Slf4j
public class DeadlineCondition implements FinishableCondition {

	private final String keyPrefix;
	private final Date deadline;
	private final RedisConnectionFactory redisConnectionFactory;
	private final Map<Long, RedisTemplate<String, Long>> timestampMap;
	private final AtomicBoolean finished;

	public DeadlineCondition(String keyPrefix, RedisConnectionFactory redisConnectionFactory, long delay, TimeUnit timeUnit) {
		this(keyPrefix, redisConnectionFactory, new Date(System.currentTimeMillis() + DateUtils.convertToMillis(delay, timeUnit)));
	}

	public DeadlineCondition(String keyPrefix, RedisConnectionFactory redisConnectionFactory, Date deadline) {
		if (deadline != null && deadline.before(new Date())) {
			throw new IllegalArgumentException("Invalid deadine date: " + deadline);
		}
		this.keyPrefix = keyPrefix;
		this.redisConnectionFactory = redisConnectionFactory;
		this.deadline = deadline;
		this.timestampMap = new ConcurrentHashMap<Long, RedisTemplate<String, Long>>();
		this.finished = new AtomicBoolean(false);
	}

	@Override
	public boolean mightFinish(Tuple tuple) {
		Long catalogId = (Long) tuple.getField("catalogId");
		String key = keyPrefix + catalogId;
		RedisTemplate<String, Long> redisTemplate = MapUtils.get(timestampMap, catalogId, () -> {
			return getLong();
		});
		redisTemplate.opsForValue().set(key, System.currentTimeMillis());
		if (finished.getAndSet(redisTemplate.opsForValue().get(key) > deadline.getTime())) {
			if (timestampMap.containsKey(catalogId)) {
				long timestamp = redisTemplate.opsForValue().get(key);
				log.info("Finish crawling work on deadline: {}", new Date(timestamp));
				redisTemplate.expire(key, 60, TimeUnit.SECONDS);
				timestampMap.remove(catalogId);
			}
		}
		return isFinished();
	}

	@Override
	public boolean isFinished() {
		return finished.get();
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
