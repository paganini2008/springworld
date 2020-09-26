package com.github.paganini2008.springworld.redisplus.messager;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

/**
 * 
 * RedisMessageSender
 * 
 * @author Fred Feng
 * @version 1.0
 */
public class RedisMessageSender {

	@Value("${spring.redis.messager.ephemeral-key.namespace:ephemeral-message:}")
	private String namespace;

	@Autowired
	private RedisMessageDispatcher redisMessageDispather;

	@Autowired
	private RedisMessageEventListener redisMessageListener;

	public void sendMessage(RedisMessageEntity messageEntity) {
		redisMessageDispather.dispatch(messageEntity);
	}

	public void sendMessage(String channel, Object message) {
		sendMessage(createEntity(channel, message));
	}

	private RedisMessageEntity createEntity(String channel, Object message) {
		return RedisMessageEntity.of(channel, message);
	}

	public void sendEphemeralMessage(String channel, Object message, long delay, TimeUnit timeUnit) {
		final String expiredKey = namespace + channel;
		RedisMessageEntity messageEntity = createEntity(channel, message);
		redisMessageDispather.expire(expiredKey, messageEntity, delay, timeUnit);
	}

	public void subscribeChannel(String beanName, RedisMessageHandler messageHandler) {
		redisMessageListener.addHandler(beanName, messageHandler);
	}

	public void unsubscribeChannel(String beanName) {
		redisMessageListener.removeHandler(beanName);
	}

}
