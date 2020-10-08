package com.github.paganini2008.springworld.reditools.messager;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;

import com.github.paganini2008.springworld.reditools.BeanNames;

/**
 * 
 * QueueRedisMessageDispatcher
 *
 * @author Fred Feng
 *
 * @since 1.0
 */
public class QueueRedisMessageDispatcher implements RedisMessageDispatcher {

	@Value("${spring.redis.messager.queue:messager-queue}")
	private String queueKey;

	@Value("${spring.redis.messager.queue.channel:messager-queue}")
	private String queueChannelKey;

	@Autowired
	@Qualifier(BeanNames.REDIS_TEMPLATE)
	private RedisTemplate<String, Object> redisTemplate;

	@Override
	public void dispatch(RedisMessageEntity messageEntity) {
		redisTemplate.opsForList().rightPush(queueKey, messageEntity);
		redisTemplate.convertAndSend(queueChannelKey, messageEntity);
	}

	@Override
	public void expire(String expiredKey, RedisMessageEntity messageEntity, long delay, TimeUnit timeUnit) {
		redisTemplate.opsForList().rightPush(queueKey, messageEntity);
		redisTemplate.opsForValue().set(expiredKey, messageEntity, delay, timeUnit);
	}

}
