package com.github.paganini2008.springworld.redisplus.messager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;

import com.github.paganini2008.springworld.redisplus.BeanNames;

/**
 * 
 * PubSubRedisMessageDispatcher
 *
 * @author Fred Feng
 *
 * @since 1.0
 */
public class PubSubRedisMessageDispatcher implements RedisMessageDispatcher {

	@Value("${spring.redis.messager.pubsub.channel:messager-pubsub}")
	private String pubsubChannelKey;

	@Autowired
	@Qualifier(BeanNames.REDIS_TEMPLATE)
	private RedisTemplate<String, Object> redisTemplate;

	@Override
	public void dispatch(RedisMessageEntity messageEntity) {
		redisTemplate.convertAndSend(this.pubsubChannelKey, messageEntity);
	}

}
