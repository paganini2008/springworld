package com.github.paganini2008.springworld.cluster.election;

import java.util.concurrent.TimeUnit;

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
import com.github.paganini2008.springworld.cluster.ApplicationClusterNewLeaderEvent;
import com.github.paganini2008.springworld.cluster.ApplicationClusterRefreshedEvent;
import com.github.paganini2008.springworld.cluster.ApplicationInfo;
import com.github.paganini2008.springworld.cluster.InstanceId;
import com.github.paganini2008.springworld.reditools.BeanNames;
import com.github.paganini2008.springworld.reditools.common.TtlKeeper;

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

	@Value("${spring.application.cluster.name:default}")
	private String clusterName;

	@Value("${spring.application.cluster.leader.timeout:5}")
	private int leaderTimeout;

	@Autowired
	private InstanceId instanceId;

	@Qualifier(BeanNames.REDIS_TEMPLATE)
	@Autowired
	private RedisTemplate<String, Object> redisTemplate;

	@Autowired
	private TtlKeeper ttlKeeper;

	private ApplicationContext applicationContext;

	@Override
	public void lookupLeader(ApplicationEvent applicationEvent) {
		final String key = ApplicationClusterAware.APPLICATION_CLUSTER_NAMESPACE + clusterName;
		if (redisTemplate.hasKey(key) && redisTemplate.getExpire(key) < 0) {
			redisTemplate.delete(key);
		}
		final ApplicationInfo myInfo = instanceId.getApplicationInfo();
		redisTemplate.opsForList().leftPush(key, myInfo);
		ApplicationInfo leaderInfo;
		if (myInfo.equals(leaderInfo = (ApplicationInfo) redisTemplate.opsForList().index(key, -1))) {
			applicationContext.publishEvent(new ApplicationClusterNewLeaderEvent(applicationContext));
			log.info("I am the leader of application cluster '{}'. Implement ApplicationListener to listen event type {}", clusterName,
					ApplicationClusterNewLeaderEvent.class.getName());
		} else {
			applicationContext.publishEvent(new ApplicationClusterFollowerEvent(applicationContext, leaderInfo));
			log.info("I am the follower of application cluster '{}'. Implement ApplicationListener to listen the event type {}",
					clusterName, ApplicationClusterFollowerEvent.class.getName());
		}
		leaderInfo.setLeader(true);
		instanceId.setLeaderInfo(leaderInfo);
		log.info("Leader's info: " + leaderInfo);

		// Update lead info from redis
		long size = redisTemplate.opsForList().size(key);
		ApplicationInfo info;
		for (int i = 0; i < size; i++) {
			info = (ApplicationInfo) redisTemplate.opsForList().index(key, i);
			if (info.equals(leaderInfo)) {
				info.setLeader(true);
				redisTemplate.opsForList().set(key, i, info);
			} else if (info.equals(myInfo)) {
				info.setLeaderInfo(leaderInfo);
				redisTemplate.opsForList().set(key, i, info);
			}
		}

		if (instanceId.isLeader()) {
			ttlKeeper.keep(key, leaderTimeout, TimeUnit.SECONDS);
		}
		
		applicationContext.publishEvent(new ApplicationClusterRefreshedEvent(applicationContext));
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

}
