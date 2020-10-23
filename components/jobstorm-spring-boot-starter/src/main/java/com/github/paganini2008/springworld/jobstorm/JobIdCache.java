package com.github.paganini2008.springworld.jobstorm;

import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * 
 * JobIdCache
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class JobIdCache {

	public JobIdCache(RedisConnectionFactory redisConnectionFactory, RedisSerializer<?> redisSerializer) {
		redisTemplate = new RedisTemplate<JobKey, Integer>();
		redisTemplate.setKeySerializer(redisSerializer);
		redisTemplate.setValueSerializer(new GenericToStringSerializer<Integer>(Integer.class));
		redisTemplate.setExposeConnection(true);
		redisTemplate.setConnectionFactory(redisConnectionFactory);
		redisTemplate.afterPropertiesSet();
	}

	private final RedisTemplate<JobKey, Integer> redisTemplate;

	public int getJobId(JobKey jobKey, JobIdSupplier supplier) throws SQLException {
		if (!redisTemplate.hasKey(jobKey)) {
			redisTemplate.opsForValue().set(jobKey, supplier.get(), 1, TimeUnit.HOURS);
		}
		return redisTemplate.opsForValue().get(jobKey).intValue();
	}

	public void evict(JobKey jobKey) {
		if (redisTemplate.hasKey(jobKey)) {
			redisTemplate.delete(jobKey);
		}
	}

	@FunctionalInterface
	public interface JobIdSupplier {

		int get() throws SQLException;

	}

}
