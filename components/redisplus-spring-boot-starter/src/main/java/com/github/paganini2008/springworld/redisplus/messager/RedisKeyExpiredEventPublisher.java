package com.github.paganini2008.springworld.redisplus.messager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.data.redis.core.RedisKeyExpiredEvent;
import org.springframework.data.redis.core.RedisTemplate;

import com.github.paganini2008.devtools.CharsetUtils;
import com.github.paganini2008.springworld.redisplus.BeanNames;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * RedisKeyExpiredEventPublisher
 *
 * @author Fred Feng
 * @version 1.0
 */
@Slf4j
@SuppressWarnings("all")
public class RedisKeyExpiredEventPublisher implements ApplicationListener<RedisKeyExpiredEvent>, ApplicationContextAware {

	private final ConcurrentMap<String, Map<String, RedisMessageHandler>> channelHandlers = new ConcurrentHashMap<String, Map<String, RedisMessageHandler>>();
	private final ConcurrentMap<String, Map<String, RedisMessageHandler>> channelPatternHandlers = new ConcurrentHashMap<String, Map<String, RedisMessageHandler>>();

	@Autowired
	private RedisMessageSender redisMessageSender;

	@Qualifier(BeanNames.REDIS_TEMPLATE)
	@Autowired
	private RedisTemplate<String, Object> redisTemplate;

	@Value("${spring.redis.messager.ephemeral-key.namespace:ephemeral-message:}")
	private String namespace;

	public void onApplicationEvent(RedisKeyExpiredEvent event) {
		final String expiredKey = new String(event.getSource(), CharsetUtils.UTF_8);
		if (expiredKey.startsWith(namespace)) {
			final String channel = expiredKey.replace(namespace, "");
			RedisMessageEntity redisMessageEntity = getRedisMessageEntity(expiredKey);
			if (redisMessageEntity != null) {
				if (log.isTraceEnabled()) {
					log.trace("Redis key '{}' is expired. The value is '{}'", expiredKey, redisMessageEntity);
				}
				applicationContext.publishEvent(new RedisMessageEvent(redisMessageEntity));
			}
		}
	}

	private RedisMessageEntity getRedisMessageEntity(String expiredKey) {
		final String key = RedisMessageSender.EXPIRED_KEY_PREFIX + expiredKey;
		if (redisTemplate.hasKey(key)) {
			RedisMessageEntity entity = (RedisMessageEntity) redisTemplate.opsForValue().get(key);
			redisTemplate.delete(key);
			return entity;
		}
		return null;
	}

	private ApplicationContext applicationContext;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
}
