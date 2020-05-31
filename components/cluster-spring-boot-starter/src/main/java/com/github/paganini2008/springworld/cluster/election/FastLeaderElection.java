package com.github.paganini2008.springworld.cluster.election;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.data.redis.core.RedisTemplate;

import com.github.paganini2008.springworld.cluster.ApplicationClusterAware;
import com.github.paganini2008.springworld.cluster.ApplicationClusterFollowerEvent;
import com.github.paganini2008.springworld.cluster.ApplicationClusterHeartbeatThread;
import com.github.paganini2008.springworld.cluster.ApplicationClusterNewLeaderEvent;
import com.github.paganini2008.springworld.cluster.ApplicationInfo;
import com.github.paganini2008.springworld.cluster.InstanceId;
import com.github.paganini2008.springworld.redisplus.BeanNames;

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

	@Qualifier(BeanNames.REDIS_TEMPLATE)
	@Autowired
	private RedisTemplate<String, Object> redisTemplate;

	@Autowired
	private ApplicationClusterHeartbeatThread clusterHeartbeatThread;

	private ApplicationContext applicationContext;

	@Override
	public void lookupLeader(ApplicationEvent applicationEvent) {
		final String key = ApplicationClusterAware.APPLICATION_CLUSTER_NAMESPACE + applicationName;
		if (redisTemplate.hasKey(key) && redisTemplate.getExpire(key) < 0) {
			redisTemplate.delete(key);
		}
		final ApplicationInfo myInfo = instanceId.getApplicationInfo();
		redisTemplate.opsForList().leftPush(key, myInfo);
		ApplicationInfo leaderInfo;
		if (myInfo.equals(leaderInfo = (ApplicationInfo) redisTemplate.opsForList().index(key, -1))) {
			clusterHeartbeatThread.start();
			applicationContext.publishEvent(new ApplicationClusterNewLeaderEvent(applicationContext));
			log.info("I am the leader of application cluster '{}'. Implement ApplicationListener to listen event type {}", applicationName,
					ApplicationClusterNewLeaderEvent.class.getName());
		} else {
			applicationContext.publishEvent(new ApplicationClusterFollowerEvent(applicationContext, leaderInfo));
			log.info("I am the follower of application cluster '{}'. Implement ApplicationListener to listen the event type {}",
					applicationName, ApplicationClusterFollowerEvent.class.getName());
		}
		log.info("Leader's info: " + leaderInfo);
		instanceId.setLeaderInfo(leaderInfo);
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

}
