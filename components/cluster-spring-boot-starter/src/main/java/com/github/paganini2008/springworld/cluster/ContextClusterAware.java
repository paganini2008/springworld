package com.github.paganini2008.springworld.cluster;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.data.redis.core.StringRedisTemplate;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * ContextClusterAware
 * 
 * @author Fred Feng
 * 
 * 
 * @version 1.0
 */
@Slf4j
public class ContextClusterAware implements ApplicationListener<ContextRefreshedEvent> {
	
	public static final String SPRING_CLUSTER_NAMESPACE = "spring:application:cluster:";
	
	@Value("${spring.application.name}")
	private String applicationName;

	@Autowired
	private ClusterId clusterId;

	@Autowired
	private StringRedisTemplate redisTemplate;

	@Autowired
	private ContextClusterHeartbeatThread heartbeatThread;

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		final String key = SPRING_CLUSTER_NAMESPACE + applicationName;
		if (redisTemplate.hasKey(key) && redisTemplate.getExpire(key) < 0) {
			redisTemplate.delete(key);
		}

		final ApplicationContext context = event.getApplicationContext();
		final String id = clusterId.get();
		redisTemplate.opsForList().leftPush(key, id);
		String masterId;
		if (id.equals(masterId = redisTemplate.opsForList().index(key, -1))) {
			clusterId.setMaster(true);
			heartbeatThread.start();
			context.publishEvent(new ContextMasterStandbyEvent(context));
			log.info("Master of context cluster '{}' is you. You can also implement ApplicationListener to listen the event type {}",
					applicationName, ContextMasterStandbyEvent.class.getName());
		} else {
			context.publishEvent(new ContextSlaveStandbyEvent(context, masterId));
			log.info("Slave of context cluster '{}' is you. You can also implement ApplicationListener to listen the event type {}",
					applicationName, ContextSlaveStandbyEvent.class.getName());
		}
	}

}
