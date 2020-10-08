package com.github.paganini2008.springworld.reditools.messager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.data.redis.core.RedisKeyExpiredEvent;

import com.github.paganini2008.devtools.CharsetUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * QueueRedisKeyExpiredEventListener
 *
 * @author Fred Feng
 *
 * @since 1.0
 */
@Slf4j
@SuppressWarnings("all")
public class QueueRedisKeyExpiredEventListener implements ApplicationListener<RedisKeyExpiredEvent> {

	@Autowired
	private RedisMessageEventPublisher eventPublisher;

	@Value("${spring.redis.messager.ephemeral-key.namespace:ephemeral-message:}")
	private String namespace;

	public void onApplicationEvent(RedisKeyExpiredEvent event) {
		final String expiredKey = new String(event.getSource(), CharsetUtils.UTF_8);
		if (expiredKey.startsWith(namespace)) {
			if (log.isTraceEnabled()) {
				log.trace("Redis key '{}' is expired.", expiredKey);
			}
			RedisMessageEntity redisMessageEntity = getRedisMessageEntity(expiredKey);
			eventPublisher.doQueue(redisMessageEntity);
		}
	}

	protected RedisMessageEntity getRedisMessageEntity(String expiredKey) {
		return RedisMessageEntity.EMPTY;
	}

}
