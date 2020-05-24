package com.github.paganini2008.springworld.cluster.consistency;

import org.springframework.beans.factory.annotation.Autowired;

import com.github.paganini2008.springworld.cluster.InstanceId;
import com.github.paganini2008.springworld.cluster.multicast.ClusterMulticastEventListener;
import com.github.paganini2008.springworld.cluster.multicast.ClusterMulticastGroup;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * LeaderElection
 *
 * @author Fred Feng
 * @since 1.0
 */
@Slf4j
public class LeaderElection implements ClusterMulticastEventListener {

	@Autowired
	private ConsistencyRequestContext context;

	@Autowired
	private ClusterMulticastGroup contextMulticastGroup;

	@Autowired
	private InstanceId clusterId;

	@Override
	public void onActive(String instanceId) {
		int channels = contextMulticastGroup.countOfChannel();
		if (channels >= 3) {
			log.info("Start leader election ...");
			context.propose("Leader", clusterId.get());
		}
	}

}
