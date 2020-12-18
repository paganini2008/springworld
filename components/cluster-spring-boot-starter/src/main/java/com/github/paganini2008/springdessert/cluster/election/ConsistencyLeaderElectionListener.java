package com.github.paganini2008.springdessert.cluster.election;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.redis.core.RedisTemplate;

import com.github.paganini2008.springdessert.cluster.Constants;
import com.github.paganini2008.springdessert.cluster.ApplicationClusterFollowerEvent;
import com.github.paganini2008.springdessert.cluster.ApplicationInfo;
import com.github.paganini2008.springdessert.cluster.InstanceId;
import com.github.paganini2008.springdessert.cluster.LeaderRecovery;
import com.github.paganini2008.springdessert.cluster.multicast.ApplicationMulticastGroup;
import com.github.paganini2008.springdessert.cluster.multicast.ApplicationMulticastListener;
import com.github.paganini2008.springdessert.reditools.BeanNames;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * ConsistencyLeaderElectionListener
 *
 * @author Jimmy Hoff
 * @since 1.0
 */
@Slf4j
public class ConsistencyLeaderElectionListener implements ApplicationMulticastListener, ApplicationContextAware, LeaderElectionListener {

	@Value("${spring.application.cluster.name}")
	private String clusterName;

	@Value("${spring.application.cluster.consistency.leader-election.minimumParticipants:3}")
	private int minimumParticipants;

	@Qualifier(BeanNames.REDIS_TEMPLATE)
	@Autowired
	private RedisTemplate<String, Object> redisTemplate;

	@Autowired
	private InstanceId instanceId;

	@Autowired
	private LeaderElection leaderElection;

	@Autowired
	private ApplicationMulticastGroup multicastGroup;

	@Autowired
	private LeaderRecovery recoveryCallback;

	private ApplicationContext applicationContext;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	@Override
	public synchronized void onActive(ApplicationInfo applicationInfo) {
		if (instanceId.getLeaderInfo() != null) {
			return;
		}
		ApplicationInfo leaderInfo = applicationInfo.getLeaderInfo();
		if (leaderInfo != null) {
			log.info("Join the existed cluster: " + leaderInfo.getClusterName());

			applicationContext.publishEvent(new ApplicationClusterFollowerEvent(applicationContext, leaderInfo));
			log.info("I am the follower of application cluster '{}'. Implement ApplicationListener to listen the event type {}",
					clusterName, ApplicationClusterFollowerEvent.class.getName());
			instanceId.setLeaderInfo(leaderInfo);
			log.info("Leader's info: " + leaderInfo);

			final String key = Constants.APPLICATION_CLUSTER_NAMESPACE + clusterName;
			redisTemplate.opsForList().leftPush(key, instanceId.getApplicationInfo());
		} else {
			final int channelCount = multicastGroup.countOfCandidate();
			if (channelCount >= minimumParticipants) {
				leaderElection.launch();
			}
		}
	}

	@Override
	public synchronized void onInactive(ApplicationInfo applicationInfo) {
		if (applicationInfo.isLeader()) {
			final String key = Constants.APPLICATION_CLUSTER_NAMESPACE + clusterName;
			redisTemplate.opsForList().remove(key, 1, instanceId.getApplicationInfo());

			instanceId.setLeaderInfo(null);

			recoveryCallback.recover(applicationInfo);
		}
	}

}
