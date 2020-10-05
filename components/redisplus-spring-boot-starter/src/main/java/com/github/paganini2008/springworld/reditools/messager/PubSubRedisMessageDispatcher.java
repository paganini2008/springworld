package com.github.paganini2008.springworld.reditools.messager;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;

import com.github.paganini2008.springworld.reditools.BeanNames;

/**
 * 
 * PubSubRedisMessageDispatcher
 *
 * @author Fred Feng
 *
 * @since 1.0
 */
public class PubSubRedisMessageDispatcher implements RedisMessageDispatcher {

	static final String EXPIRED_KEY_PREFIX = "__";

	@Value("${spring.redis.messager.pubsub.channel:messager-pubsub}")
	private String pubsubChannelKey;

	@Autowired
	@Qualifier(BeanNames.REDIS_TEMPLATE)
	private RedisTemplate<String, Object> redisTemplate;

	@Override
	public void dispatch(RedisMessageEntity messageEntity) {
		redisTemplate.convertAndSend(this.pubsubChannelKey, messageEntity);
	}

	@Override
	public void expire(String expiredKey, RedisMessageEntity messageEntity, long delay, TimeUnit timeUnit) {
		final String key = EXPIRED_KEY_PREFIX + expiredKey;
		redisTemplate.opsForValue().set(key, messageEntity);
		redisTemplate.opsForValue().set(expiredKey, messageEntity, delay, timeUnit);
	}

}
