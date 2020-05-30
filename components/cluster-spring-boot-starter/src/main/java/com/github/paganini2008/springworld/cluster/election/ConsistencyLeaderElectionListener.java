package com.github.paganini2008.springworld.cluster.election;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.github.paganini2008.springworld.cluster.ApplicationClusterAware;
import com.github.paganini2008.springworld.cluster.ApplicationClusterFollowerEvent;
import com.github.paganini2008.springworld.cluster.ApplicationInfo;
import com.github.paganini2008.springworld.cluster.InstanceId;
import com.github.paganini2008.springworld.cluster.consistency.ConsistencyRequestContext;
import com.github.paganini2008.springworld.cluster.multicast.ClusterMulticastGroup;
import com.github.paganini2008.springworld.cluster.multicast.ClusterStateChangeListener;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * ConsistencyLeaderElectionListener
 *
 * @author Fred Feng
 * @since 1.0
 */
@Slf4j
public class ConsistencyLeaderElectionListener implements ClusterStateChangeListener, ApplicationContextAware {

	private static final int LEADER_ELECTION_TIMEOUT = 30;

	@Autowired
	private ConsistencyRequestContext context;

	@Autowired
	private ClusterMulticastGroup clusterMulticastGroup;

	@Autowired
	private InstanceId instanceId;

	@Value("${spring.application.name}")
	private String applicationName;

	@Value("${spring.application.cluster.consistency.leader-election.minimumParticipants:3}")
	private int minimumParticipants;

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
			instanceId.setLeaderInfo(leaderInfo);
			applicationContext.publishEvent(new ApplicationClusterFollowerEvent(applicationContext, leaderInfo));
			log.info("I am the follower of application cluster '{}'. Implement ApplicationListener to listen the event type {}",
					applicationName, ApplicationClusterFollowerEvent.class.getName());
		} else {
			final int channelCount = clusterMulticastGroup.countOfChannel();
			if (channelCount >= minimumParticipants) {
				final String leaderIdentify = ApplicationClusterAware.APPLICATION_CLUSTER_NAMESPACE + applicationName + ":leader";
				log.info("Start leader election. Identify: " + leaderIdentify);
				context.propose(leaderIdentify, instanceId.getApplicationInfo(), LEADER_ELECTION_TIMEOUT);
			}
		}
	}

	@Override
	public synchronized void onInactive(ApplicationInfo applicationInfo) {
		if (applicationInfo.isLeader()) {
			instanceId.setLeaderInfo(null);
			final String leaderIdentify = ApplicationClusterAware.APPLICATION_CLUSTER_NAMESPACE + applicationName + ":leader";
			log.info("Start leader election. Identify: " + leaderIdentify);
			context.propose(leaderIdentify, instanceId.getApplicationInfo(), LEADER_ELECTION_TIMEOUT);
		}
	}

}
