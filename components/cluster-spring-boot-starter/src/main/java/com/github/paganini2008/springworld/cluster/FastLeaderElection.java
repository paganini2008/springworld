package com.github.paganini2008.springworld.cluster;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.data.redis.core.StringRedisTemplate;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * FastLeaderElection
 *
 * @author Fred Feng
 * @since 1.0
 */
@Slf4j
public class FastLeaderElection implements LeaderElection, ApplicationContextAware {

	@Value("${spring.application.name}")
	private String applicationName;

	@Autowired
	private InstanceId instanceId;

	@Autowired
	private StringRedisTemplate redisTemplate;

	@Autowired
	private ApplicationClusterHeartbeatThread clusterHeartbeatThread;

	private ApplicationContext applicationContext;

	@Override
	public void lookupLeader(ApplicationEvent applicationEvent) {
		log.info("Lookup leader for application cluster '{}'", applicationName);
		final String key = ApplicationClusterAware.APPLICATION_CLUSTER_NAMESPACE + applicationName;
		if (redisTemplate.hasKey(key) && redisTemplate.getExpire(key) < 0) {
			redisTemplate.delete(key);
		}
		final String me = instanceId.get();
		redisTemplate.opsForList().leftPush(key, me);
		String leaderId;
		if (me.equals(leaderId = redisTemplate.opsForList().index(key, -1))) {
			clusterHeartbeatThread.start();
			applicationContext.publishEvent(new ApplicationClusterNewLeaderEvent(applicationContext));
			log.info("You are the leader of application cluster '{}'. Implement ApplicationListener to listen event type {}",
					applicationName, ApplicationClusterNewLeaderEvent.class.getName());
		} else {
			applicationContext.publishEvent(new ApplicationClusterFollowerEvent(applicationContext, leaderId));
			log.info("You are the follower of application cluster '{}'. Implement ApplicationListener to listen the event type {}",
					applicationName, ApplicationClusterFollowerEvent.class.getName());
		}
		instanceId.setLeaderId(leaderId);
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

}
