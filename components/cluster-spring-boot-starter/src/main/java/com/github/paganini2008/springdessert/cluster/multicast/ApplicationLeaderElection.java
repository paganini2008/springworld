package com.github.paganini2008.springdessert.cluster.multicast;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;

import com.github.paganini2008.devtools.ArrayUtils;
import com.github.paganini2008.springdessert.cluster.ApplicationInfo;
import com.github.paganini2008.springdessert.cluster.InstanceId;
import com.github.paganini2008.springdessert.cluster.election.ApplicationClusterFollowerEvent;
import com.github.paganini2008.springdessert.cluster.election.ApplicationClusterLeaderEvent;
import com.github.paganini2008.springdessert.cluster.election.ApplicationClusterRefreshedEvent;
import com.github.paganini2008.springdessert.cluster.election.LeaderElection;
import com.github.paganini2008.springdessert.cluster.election.LeaderNotFoundException;
import com.github.paganini2008.springdessert.cluster.multicast.ApplicationMulticastEvent.MulticastEventType;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * ApplicationLeaderElection
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
@Slf4j
public class ApplicationLeaderElection implements LeaderElection, ApplicationContextAware {

	@Value("${spring.application.cluster.name}")
	private String clusterName;

	@Autowired
	private ApplicationMulticastGroup applicationMulticastGroup;

	@Autowired
	private InstanceId instanceId;

	private ApplicationContext applicationContext;

	@Override
	public synchronized void launch() {
		if (instanceId.getLeaderInfo() != null) {
			return;
		}
		ApplicationInfo[] candidates = applicationMulticastGroup.getCandidates();
		if (ArrayUtils.isEmpty(candidates)) {
			throw new LeaderNotFoundException("No candidates for election");
		}
		ApplicationInfo leader;
		ApplicationInfo self = instanceId.getApplicationInfo();
		if ((leader = candidates[0]).equals(self)) {
			applicationContext.publishEvent(new ApplicationClusterLeaderEvent(applicationContext));
			log.info("This is the leader of application cluster '{}'. Current application event type is '{}'", clusterName,
					ApplicationClusterLeaderEvent.class.getName());
		} else {
			applicationContext.publishEvent(new ApplicationClusterFollowerEvent(applicationContext, leader));
			log.info("This is the follower of application cluster '{}'. Current application event type is '{}'", clusterName,
					ApplicationClusterFollowerEvent.class.getName());
		}
		leader.setLeader(true);
		instanceId.setLeaderInfo(leader);
		log.info("Current leader: " + leader);
		
		applicationContext.publishEvent(new ApplicationClusterRefreshedEvent(applicationContext, leader));
	}

	@Override
	public void onTriggered(ApplicationEvent applicationEvent) {
		ApplicationMulticastEvent applicationMulticastEvent = (ApplicationMulticastEvent) applicationEvent;
		MulticastEventType eventType = applicationMulticastEvent.getMulticastEventType();
		if (eventType == MulticastEventType.ON_ACTIVE) {
			launch();
		}
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

}
