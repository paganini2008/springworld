package com.github.paganini2008.springdessert.reditools.messager;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.redis.core.RedisTemplate;

import com.github.paganini2008.springdessert.reditools.BeanNames;

/**
 * 
 * RedisMessageEventPublisher
 *
 * @author Fred Feng
 *
 * @since 1.0
 */
public class RedisMessageEventPublisher implements ApplicationContextAware {

	@Value("${spring.redis.messager.queue:messager-queue}")
	private String queueKey;

	@Autowired
	@Qualifier(BeanNames.REDIS_TEMPLATE)
	private RedisTemplate<String, Object> redisTemplate;

	public void doQueue(RedisMessageEntity entity) {
		RedisMessageEntity messageEntity = (RedisMessageEntity) redisTemplate.opsForList().leftPop(queueKey);
		if (messageEntity != null) {
			applicationContext.publishEvent(new RedisMessageEvent(messageEntity));
		}
	}

	public void doPubsub(RedisMessageEntity messageEntity) {
		applicationContext.publishEvent(new RedisMessageEvent(messageEntity));
	}

	private ApplicationContext applicationContext;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

}
