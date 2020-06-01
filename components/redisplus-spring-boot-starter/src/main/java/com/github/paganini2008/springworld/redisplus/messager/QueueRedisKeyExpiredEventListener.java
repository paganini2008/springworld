package com.github.paganini2008.springworld.redisplus.messager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

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
 * QueueRedisKeyExpiredEventListener
 *
 * @author Fred Feng
 *
 * @since 1.0
 */
@Slf4j
@SuppressWarnings("all")
public class QueueRedisKeyExpiredEventListener implements ApplicationListener<RedisKeyExpiredEvent> {

	private final ConcurrentMap<String, Map<String, RedisMessageHandler>> channelHandlers = new ConcurrentHashMap<String, Map<String, RedisMessageHandler>>();
	private final ConcurrentMap<String, Map<String, RedisMessageHandler>> channelPatternHandlers = new ConcurrentHashMap<String, Map<String, RedisMessageHandler>>();

	@Autowired
	private RedisMessageSender redisMessageSender;

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
			eventPublisher.doQueue(redisMessageEntity);
		}
	}

	protected RedisMessageEntity getRedisMessageEntity(String expiredKey) {
		return RedisMessageEntity.EMPTY;
	}

}
