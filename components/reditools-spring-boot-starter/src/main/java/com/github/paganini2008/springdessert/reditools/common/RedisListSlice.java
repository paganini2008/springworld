package com.github.paganini2008.springdessert.reditools.common;

import java.util.List;

import org.springframework.data.redis.core.RedisTemplate;

import com.github.paganini2008.devtools.jdbc.ResultSetSlice;

/**
 * 
 * RedisListSlice
 *
 * @author Jimmy Hoff
 * 
 * 
 * @version 1.0
 */
public class RedisListSlice<T> implements ResultSetSlice<T> {

	private final String key;
	private final RedisTemplate<String, Object> redisTemplate;

	public RedisListSlice(String key, RedisTemplate<String, Object> redisTemplate) {
		this.key = key;
		this.redisTemplate = redisTemplate;
	}

	@Override
	public int rowCount() {
		Number number = redisTemplate.opsForList().size(key);
		return number != null ? number.intValue() : 0;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<T> list(int maxResults, int firstResult) {
		return (List<T>) redisTemplate.opsForList().range(key, firstResult, firstResult + maxResults - 1);
	}

}
