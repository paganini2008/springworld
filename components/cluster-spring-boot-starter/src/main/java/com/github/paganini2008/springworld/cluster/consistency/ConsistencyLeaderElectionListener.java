package com.github.paganini2008.springworld.cluster.consistency;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.github.paganini2008.springworld.cluster.ApplicationClusterAware;
import com.github.paganini2008.springworld.cluster.InstanceId;
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
public class ConsistencyLeaderElectionListener implements ClusterStateChangeListener {

	private static final int MINIMUM_LEADER_ELECTION_PARTICIPANTS = 3;

	@Autowired
	private ConsistencyRequestContext context;

	@Autowired
	private ClusterMulticastGroup clusterMulticastGroup;

	@Autowired
	private InstanceId instanceId;

	@Value("${spring.application.name}")
	private String applicationName;

	@Override
	public void onActive(String anotherInstanceId) {
		final int channels = clusterMulticastGroup.countOfChannel();
		if (channels >= MINIMUM_LEADER_ELECTION_PARTICIPANTS) {
			final String leaderIdentify = ApplicationClusterAware.APPLICATION_CLUSTER_NAMESPACE + applicationName + ":leader";
			log.info("Start leader election. Identify: " + leaderIdentify);
			context.propose(leaderIdentify, instanceId.get());
		}
	}

	@Override
	public void onInactive(String anotherInstanceId) {
		if (instanceId.getLeaderId().equals(anotherInstanceId)) {
			final String leaderIdentify = ApplicationClusterAware.APPLICATION_CLUSTER_NAMESPACE + applicationName + ":leader";
			log.info("Start leader election. Identify: " + leaderIdentify);
			context.propose(leaderIdentify, instanceId.get());
		}
	}

}
