package com.github.paganini2008.springdessert.reditools.common;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * 
 * GenericRedisTemplate
 *
 * @author Jimmy Hoff
 * 
 * @since 1.0
 */
@SuppressWarnings("all")
public class GenericRedisTemplate<T> extends RedisTemplate<String, T> {

	public GenericRedisTemplate(String key, Class<T> valueClass, RedisConnectionFactory redisConnectionFactory) {
		this(key, valueClass, redisConnectionFactory, null);
	}

	public GenericRedisTemplate(String key, Class<T> valueClass, RedisConnectionFactory redisConnectionFactory, T defaultValue) {
		this.key = key;
		setKeySerializer(RedisSerializer.string());
		setValueSerializer(new GenericToStringSerializer<T>(valueClass));
		setHashKeySerializer(RedisSerializer.string());
		setHashValueSerializer(new GenericToStringSerializer<T>(valueClass));
		setExposeConnection(true);
		setConnectionFactory(redisConnectionFactory);
		afterPropertiesSet();

		if (defaultValue != null) {
			setIfAbsent(defaultValue);
		}
	}

	private final String key;

	public void set(T value) {
		opsForValue().set(key, value);
	}

	public void setIfAbsent(T value) {
		if (!hasKey(key)) {
			opsForValue().set(key, value);
		}
	}

	public void set(T value, long expiration, TimeUnit timeUnit) {
		opsForValue().set(key, value, expiration, timeUnit);
	}

	public void setIfAbsent(T value, long expiration, TimeUnit timeUnit) {
		if (!hasKey(key)) {
			opsForValue().set(key, value, expiration, timeUnit);
		}
	}

	public T get() {
		return opsForValue().get(key);
	}

	public Long leftPushList(T value) {
		return opsForList().leftPush(key, value);
	}

	public Long rightPushList(T value) {
		return opsForList().rightPush(key, value);
	}

	public T leftPopList(String key) {
		return opsForList().leftPop(key);
	}

	public T rightPopList(String key) {
		return opsForList().rightPop(key);
	}

	public T indexList(long index) {
		return opsForList().index(key, index);
	}

	public Long removeList(long count, T value) {
		return opsForList().remove(key, count, value);
	}

	public List<T> get(long start, long end) {
		return opsForList().range(key, start, end);
	}

	public long sizeList(String key) {
		Long l = opsForList().size(key);
		return l != null ? l.longValue() : 0;
	}

	public void putHash(String hashKey, T value) {
		opsForHash().put(key, hashKey, value);
	}

	public T getHash(String hashKey) {
		return (T) opsForHash().get(key, hashKey);
	}

	public List<T> multiGet(String... hashKeys) {
		return (List<T>) opsForHash().multiGet(key, Arrays.asList(hashKeys));
	}

	public Long deleteHash(String... hashKeys) {
		return opsForHash().delete(key, hashKeys);
	}
}
