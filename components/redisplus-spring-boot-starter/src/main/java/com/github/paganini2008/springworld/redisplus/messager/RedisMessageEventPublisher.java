package com.github.paganini2008.springworld.redisplus.messager;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.redis.core.RedisTemplate;

import com.github.paganini2008.springworld.redisplus.BeanNames;

/**
 * 
 * RedisMessageEventPublisher
 * 
 * @author Fred Feng
 * @version 1.0
 */
public class RedisMessageEventPublisher implements ApplicationContextAware {

	@Value("${spring.redis.messager.queue:messager-queue}")
	private String queueKey;

	@Autowired
	@Qualifier(BeanNames.REDIS_TEMPLATE)
	private RedisTemplate<String, Object> redisTemplate;

	public void queue(RedisMessageEntity entity) {
		RedisMessageEntity redisMessageEntity = (RedisMessageEntity) redisTemplate.opsForList().leftPop(queueKey);
		if (redisMessageEntity != null) {
			applicationContext.publishEvent(new RedisMessageEvent(entity));
		}
	}

	public void pubsub(RedisMessageEntity entity) {
		applicationContext.publishEvent(new RedisMessageEvent(entity));
	}

	private ApplicationContext applicationContext;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

}
