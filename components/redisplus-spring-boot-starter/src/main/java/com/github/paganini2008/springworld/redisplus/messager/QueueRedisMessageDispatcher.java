package com.github.paganini2008.springworld.redisplus.messager;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;

import com.github.paganini2008.springworld.redisplus.BeanNames;
import com.github.paganini2008.springworld.redisplus.common.TtlKeeper;

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

	@Autowired
	private TtlKeeper ttlKeeper;

	public void configure() {
		redisTemplate.opsForList().leftPush(queueKey, RedisMessageEntity.EMPTY);
		ttlKeeper.keep(queueKey, 5, TimeUnit.SECONDS);
	}

	@Override
	public void dispatch(RedisMessageEntity messageEntity) {
		redisTemplate.opsForList().leftPush(queueKey, messageEntity);
		redisTemplate.convertAndSend(queueChannelKey, messageEntity);
	}

}
