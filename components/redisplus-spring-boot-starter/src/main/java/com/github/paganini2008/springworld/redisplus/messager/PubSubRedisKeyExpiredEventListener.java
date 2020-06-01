package com.github.paganini2008.springworld.redisplus.messager;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.data.redis.core.RedisKeyExpiredEvent;
import org.springframework.data.redis.core.RedisTemplate;

import com.github.paganini2008.devtools.CharsetUtils;
import com.github.paganini2008.springworld.redisplus.BeanNames;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * PubSubRedisKeyExpiredEventListener
 *
 * @author Fred Feng
 * @version 1.0
 */
@Slf4j
@SuppressWarnings("all")
public class PubSubRedisKeyExpiredEventListener implements ApplicationListener<RedisKeyExpiredEvent> {

	@Autowired
	private RedisMessageEventPublisher eventPublisher;

	@Qualifier(BeanNames.REDIS_TEMPLATE)
	@Autowired
	private RedisTemplate<String, Object> redisTemplate;

	@Value("${spring.redis.messager.ephemeral-key.namespace:ephemeral-message:}")
	private String namespace;

	public void onApplicationEvent(RedisKeyExpiredEvent event) {
		final String expiredKey = new String(event.getSource(), CharsetUtils.UTF_8);
		if (expiredKey.startsWith(namespace)) {
			if (log.isTraceEnabled()) {
				log.trace("Redis key '{}' is expired.", expiredKey);
			}
			RedisMessageEntity redisMessageEntity = getRedisMessageEntity(expiredKey);
			eventPublisher.doPubsub(redisMessageEntity);
		}
	}

	protected RedisMessageEntity getRedisMessageEntity(String expiredKey) {
		final String key = PubSubRedisMessageDispatcher.EXPIRED_KEY_PREFIX + expiredKey;
		if (redisTemplate.hasKey(key)) {
			RedisMessageEntity entity = (RedisMessageEntity) redisTemplate.opsForValue().get(key);
			redisTemplate.expire(key, 60, TimeUnit.SECONDS);
			return entity;
		}
		return RedisMessageEntity.EMPTY;
	}

}
