package com.github.paganini2008.springworld.cluster;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.data.redis.core.RedisKeyExpiredEvent;
import org.springframework.data.redis.core.StringRedisTemplate;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * ApplicationClusterLeaderMissingListener
 * 
 * @author Fred Feng
 * @version 1.0
 */
@Slf4j
@SuppressWarnings("all")
public class ApplicationClusterLeaderMissingListener implements ApplicationListener<RedisKeyExpiredEvent>, ApplicationContextAware {

	@Autowired
	private InstanceId instanceId;

	@Value("${spring.application.name}")
	private String applicationName;

	@Autowired
	private ApplicationClusterHeartbeatThread clusterHeartbeatThread;

	@Autowired
	private StringRedisTemplate redisTemplate;

	private ApplicationContext context;

	@Override
	public void onApplicationEvent(RedisKeyExpiredEvent event) {
		final String expiredKey = new String(event.getSource());
		final String heartbeatKey = ApplicationClusterAware.APPLICATION_CLUSTER_NAMESPACE + applicationName;
		if (heartbeatKey.equals(expiredKey)) {
			log.info("The leader of application '" + applicationName + "' is missing.");
			final String key = heartbeatKey;
			redisTemplate.opsForList().leftPush(key, instanceId.get());
			String masterId;
			if (instanceId.get().equals(masterId = redisTemplate.opsForList().index(key, -1))) {
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

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.context = applicationContext;
	}

}
