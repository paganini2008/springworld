package com.github.paganini2008.springdessert.cluster.election;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.data.redis.core.RedisTemplate;

import com.github.paganini2008.springdessert.cluster.ApplicationClusterFollowerEvent;
import com.github.paganini2008.springdessert.cluster.ApplicationClusterLeaderEvent;
import com.github.paganini2008.springdessert.cluster.ApplicationClusterRefreshedEvent;
import com.github.paganini2008.springdessert.cluster.ApplicationInfo;
import com.github.paganini2008.springdessert.cluster.Constants;
import com.github.paganini2008.springdessert.cluster.InstanceId;
import com.github.paganini2008.springdessert.reditools.BeanNames;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * FastLeaderElection
 *
 * @author Jimmy Hoff
 * @since 1.0
 */
@Slf4j
public class FastLeaderElection implements LeaderElection, ApplicationContextAware {

	@Value("${spring.application.cluster.name}")
	private String clusterName;

	@Autowired
	private InstanceId instanceId;

	@Qualifier(BeanNames.REDIS_TEMPLATE)
	@Autowired
	private RedisTemplate<String, Object> redisTemplate;

	private ApplicationContext applicationContext;

	@Override
	public void launch() {
		final String key = Constants.APPLICATION_CLUSTER_NAMESPACE + clusterName;
		if (redisTemplate.hasKey(key) && redisTemplate.getExpire(key) < 0) {
			redisTemplate.delete(key);
		}
		final ApplicationInfo self = instanceId.getApplicationInfo();
		redisTemplate.opsForList().leftPush(key, self);
		ApplicationInfo leaderInfo;
		if (self.equals(leaderInfo = (ApplicationInfo) redisTemplate.opsForList().index(key, -1))) {
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
		applicationContext.publishEvent(new ApplicationClusterRefreshedEvent(applicationContext, leaderInfo));
	}

	@Override
	public void onTriggered(ApplicationEvent applicationEvent) {
		launch();
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

}
