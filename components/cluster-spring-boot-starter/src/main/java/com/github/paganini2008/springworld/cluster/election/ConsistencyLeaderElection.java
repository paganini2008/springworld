package com.github.paganini2008.springworld.cluster.election;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.SmartApplicationListener;

import com.github.paganini2008.springworld.cluster.ApplicationClusterAware;
import com.github.paganini2008.springworld.cluster.ApplicationClusterFollowerEvent;
import com.github.paganini2008.springworld.cluster.ApplicationClusterNewLeaderEvent;
import com.github.paganini2008.springworld.cluster.ApplicationInfo;
import com.github.paganini2008.springworld.cluster.InstanceId;
import com.github.paganini2008.springworld.cluster.consistency.ConsistencyRequestConfirmationEvent;

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

	@Value("${spring.application.name}")
	private String applicationName;

	@Autowired
	private InstanceId instanceId;

	@Override
	public void lookupLeader(ApplicationEvent applicationEvent) {
		ConsistencyRequestConfirmationEvent event = (ConsistencyRequestConfirmationEvent) applicationEvent;
		ApplicationInfo leaderInfo = (ApplicationInfo) event.getRequest().getValue();
		if (instanceId.getApplicationInfo().equals(leaderInfo)) {
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
		final String leaderIdentify = ApplicationClusterAware.APPLICATION_CLUSTER_NAMESPACE + applicationName + ":leader";
		if (leaderIdentify.equals(event.getRequest().getName())) {
			if (event.isOk()) {
				lookupLeader(event);
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
