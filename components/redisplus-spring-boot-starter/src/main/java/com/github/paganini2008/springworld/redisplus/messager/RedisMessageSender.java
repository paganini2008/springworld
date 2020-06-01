package com.github.paganini2008.springworld.redisplus.messager;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;

import com.github.paganini2008.springworld.redisplus.BeanNames;

/**
 * 
 * RedisMessageSender
 * 
 * @author Fred Feng
 * @version 1.0
 */
public class RedisMessageSender {

	public static final String EXPIRED_KEY_PREFIX = "__";

	@Value("${spring.redis.messager.ephemeral-key.namespace:ephemeral-message:}")
	private String namespace;

	@Autowired
	@Qualifier(BeanNames.REDIS_TEMPLATE)
	private RedisTemplate<String, Object> redisTemplate;

	@Autowired
	private RedisMessageDispatcher redisMessageDispather;

	@Autowired
	private RedisMessageEventListener redisMessageListener;

	public void sendMessage(RedisMessageEntity messageEntity) {
		redisMessageDispather.dispatch(messageEntity);
	}

	public void sendMessage(String channel, Object message) {
		RedisMessageEntity messageEntity = RedisMessageEntity.of(channel, message);
		sendMessage(messageEntity);
	}

	public void sendEphemeralMessage(String channel, Object message, long delay, TimeUnit timeUnit) {
		sendEphemeralMessage(channel, message, delay, timeUnit, false);
	}

	public void sendEphemeralMessage(String channel, Object message, long delay, TimeUnit timeUnit, boolean idempotent) {
		final String expiredKey = namespace + channel;
		if (!idempotent || redisTemplate.hasKey(expiredKey)) {
			RedisMessageEntity entity = RedisMessageEntity.of(channel, message);
			entity.setDelay(delay);
			entity.setTimeUnit(timeUnit);
			redisTemplate.opsForValue().set(expiredKey, entity, delay, timeUnit);
			setExpiredValue(expiredKey);
		}
	}

	private void setExpiredValue(String expiredKey) {
		final String key = EXPIRED_KEY_PREFIX + expiredKey;
		Object value = redisTemplate.opsForValue().get(expiredKey);
		redisTemplate.opsForValue().set(key, value);
	}

	public void subscribeChannel(final String beanName, final RedisMessageHandler messageHandler) {
		redisMessageListener.addHandler(beanName, messageHandler);
	}

	public void unsubscribeChannel(String beanName) {
		redisMessageListener.removeHandler(beanName);
	}

}
