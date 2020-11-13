package com.github.paganini2008.springworld.cluster.election;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.SmartApplicationListener;
import org.springframework.data.redis.core.RedisTemplate;

import com.github.paganini2008.springworld.cluster.ApplicationClusterAware;
import com.github.paganini2008.springworld.cluster.ApplicationClusterFollowerEvent;
import com.github.paganini2008.springworld.cluster.ApplicationClusterNewLeaderEvent;
import com.github.paganini2008.springworld.cluster.ApplicationClusterRefreshedEvent;
import com.github.paganini2008.springworld.cluster.ApplicationInfo;
import com.github.paganini2008.springworld.cluster.ClusterMode;
import com.github.paganini2008.springworld.cluster.InstanceId;
import com.github.paganini2008.springworld.cluster.consistency.ConsistencyRequestConfirmationEvent;
import com.github.paganini2008.springworld.cluster.consistency.ConsistencyRequestContext;
import com.github.paganini2008.springworld.reditools.BeanNames;
import com.github.paganini2008.springworld.reditools.common.TtlKeeper;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * ConsistencyLeaderElection
 *
 * @author Fred Feng
 * @since 1.0
 */
@Slf4j
public class ConsistencyLeaderElection implements LeaderElection, ApplicationContextAware, SmartApplicationListener {

	private ApplicationContext applicationContext;

	@Value("${spring.application.cluster.name}")
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

	@Autowired
	private ConsistencyRequestContext requestContext;

	@Override
	public void launch() {
		final String leaderIdentify = ApplicationClusterAware.APPLICATION_CLUSTER_NAMESPACE + clusterName + ":leader";
		requestContext.propose(leaderIdentify, instanceId.getApplicationInfo(), DEFAULT_TIMEOUT);
		log.info("Start leader election. Identify: " + leaderIdentify);
	}

	@Override
	public void adapt(ApplicationEvent applicationEvent) {
		final ConsistencyRequestConfirmationEvent event = (ConsistencyRequestConfirmationEvent) applicationEvent;
		ApplicationInfo leaderInfo = (ApplicationInfo) event.getRequest().getValue();
		if (instanceId.getApplicationInfo().equals(leaderInfo)) {
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

		final String key = ApplicationClusterAware.APPLICATION_CLUSTER_NAMESPACE + clusterName;
		redisTemplate.opsForList().leftPush(key, instanceId.getApplicationInfo());
		if (instanceId.isLeader()) {
			ttlKeeper.keep(key, leaderTimeout, TimeUnit.SECONDS);
		}

		instanceId.setClusterMode(ClusterMode.ACCESSABLE);
		applicationContext.publishEvent(new ApplicationClusterRefreshedEvent(applicationContext, leaderInfo));
	}

	@Override
	public boolean supportsEventType(Class<? extends ApplicationEvent> eventType) {
		return eventType == ConsistencyRequestConfirmationEvent.class;
	}

	@Override
	public boolean supportsSourceType(Class<?> sourceType) {
		return sourceType == ApplicationInfo.class;
	}

	@Override
	public void onApplicationEvent(ApplicationEvent applicationEvent) {
		final ConsistencyRequestConfirmationEvent event = (ConsistencyRequestConfirmationEvent) applicationEvent;
		final String leaderIdentify = ApplicationClusterAware.APPLICATION_CLUSTER_NAMESPACE + clusterName + ":leader";
		if (leaderIdentify.equals(event.getRequest().getName())) {
			if (event.isOk()) {
				adapt(event);
			} else {
				throw new LeaderNotFoundException();
			}
		}
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

}
