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

import com.github.paganini2008.springdessert.reditools.BeanNames;
import com.github.paganini2008.springdessert.reditools.common.TtlKeeper;
import com.github.paganini2008.springworld.cluster.ApplicationClusterAware;
import com.github.paganini2008.springworld.cluster.ApplicationClusterFollowerEvent;
import com.github.paganini2008.springworld.cluster.ApplicationClusterLeaderEvent;
import com.github.paganini2008.springworld.cluster.ApplicationClusterRefreshedEvent;
import com.github.paganini2008.springworld.cluster.ApplicationInfo;
import com.github.paganini2008.springworld.cluster.InstanceId;

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

	@Value("${spring.application.cluster.name}")
	private String clusterName;

	@Value("${spring.application.cluster.leader.lease:5}")
	private int leaderLease;

	@Autowired
	private InstanceId instanceId;

	@Qualifier(BeanNames.REDIS_TEMPLATE)
	@Autowired
	private RedisTemplate<String, Object> redisTemplate;

	@Autowired
	private TtlKeeper ttlKeeper;

	private ApplicationContext applicationContext;

	@Override
	public void launch() {
		final String key = ApplicationClusterAware.APPLICATION_CLUSTER_NAMESPACE + clusterName;
		if (redisTemplate.hasKey(key) && redisTemplate.getExpire(key) < 0) {
			redisTemplate.delete(key);
		}
		final ApplicationInfo selfInfo = instanceId.getApplicationInfo();
		redisTemplate.opsForList().leftPush(key, selfInfo);
		ApplicationInfo leaderInfo;
		if (selfInfo.equals(leaderInfo = (ApplicationInfo) redisTemplate.opsForList().index(key, -1))) {
			applicationContext.publishEvent(new ApplicationClusterLeaderEvent(applicationContext));
			log.info("This is the leader of application cluster '{}'. Current application event type is '{}'", clusterName,
					ApplicationClusterLeaderEvent.class.getName());
		} else {
			applicationContext.publishEvent(new ApplicationClusterFollowerEvent(applicationContext, leaderInfo));
			log.info("This is the follower of application cluster '{}'. Current application event type is '{}'", clusterName,
					ApplicationClusterFollowerEvent.class.getName());
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
			} else if (info.equals(selfInfo)) {
				info.setLeaderInfo(leaderInfo);
				redisTemplate.opsForList().set(key, i, info);
			}
		}

		if (instanceId.isLeader()) {
			ttlKeeper.keepAlive(key, leaderLease, 1, TimeUnit.SECONDS);
		}

		applicationContext.publishEvent(new ApplicationClusterRefreshedEvent(applicationContext, leaderInfo));
	}

	@Override
	public void adapt(ApplicationEvent applicationEvent) {
		launch();
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

}
