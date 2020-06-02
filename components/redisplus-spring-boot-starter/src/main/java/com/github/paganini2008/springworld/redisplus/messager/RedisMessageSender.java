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
		sendMessage(RedisMessageEntity.of(channel, message));
	}

	public void sendEphemeralMessage(String channel, Object message, long delay, TimeUnit timeUnit) {
		final String expiredKey = namespace + channel;
		RedisMessageEntity messageEntity = RedisMessageEntity.of(channel, message);
		messageEntity.setDelay(delay);
		messageEntity.setTimeUnit(timeUnit);
		redisMessageDispather.expire(expiredKey, messageEntity, delay, timeUnit);
	}

	public void subscribeChannel(final String beanName, final RedisMessageHandler messageHandler) {
		redisMessageListener.addHandler(beanName, messageHandler);
	}

	public void unsubscribeChannel(String beanName) {
		redisMessageListener.removeHandler(beanName);
	}

}
