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
 * ApplicationClusterAware enable the spring application to cluster mode when
 * spring context's initialization was done.
 * 
 * @author Fred Feng
 * @version 1.0
 */
@Slf4j
public class ApplicationClusterAware implements ApplicationListener<ContextRefreshedEvent> {

	public static final String APPLICATION_CLUSTER_NAMESPACE = "spring:application:cluster:";

	@Value("${spring.application.name}")
	private String applicationName;

	@Autowired
	private InstanceId instanceId;

	@Autowired
	private StringRedisTemplate redisTemplate;

	@Autowired
	private ApplicationClusterHeartbeatThread clusterHeartbeatThread;

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		final String key = APPLICATION_CLUSTER_NAMESPACE + applicationName;
		if (redisTemplate.hasKey(key) && redisTemplate.getExpire(key) < 0) {
			redisTemplate.delete(key);
		}

		final ApplicationContext context = event.getApplicationContext();
		final String id = instanceId.get();
		redisTemplate.opsForList().leftPush(key, id);
		String masterId;
		if (id.equals(masterId = redisTemplate.opsForList().index(key, -1))) {
			instanceId.setMaster(true);
			clusterHeartbeatThread.start();
			context.publishEvent(new ApplicationClusterLeaderStandbyEvent(context));
			log.info("Leader of context cluster '{}' is you. You can also implement ApplicationListener to listen the event type {}",
					applicationName, ApplicationClusterLeaderStandbyEvent.class.getName());
		} else {
			context.publishEvent(new ApplicationClusterFollowerStandbyEvent(context, masterId));
			log.info("Follower of context cluster '{}' is you. You can also implement ApplicationListener to listen the event type {}",
					applicationName, ApplicationClusterFollowerStandbyEvent.class.getName());
		}
	}

}
