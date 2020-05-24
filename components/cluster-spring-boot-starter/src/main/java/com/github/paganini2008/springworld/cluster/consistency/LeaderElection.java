package com.github.paganini2008.springworld.cluster.consistency;

import org.springframework.beans.factory.annotation.Autowired;

import com.github.paganini2008.springworld.cluster.ClusterId;
import com.github.paganini2008.springworld.cluster.multicast.ContextMulticastEventHandler;
import com.github.paganini2008.springworld.cluster.multicast.ContextMulticastGroup;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * LeaderElection
 *
 * @author Fred Feng
 * @since 1.0
 */
@Slf4j
public class LeaderElection implements ContextMulticastEventHandler {

	@Autowired
	private ConsistencyRequestContext context;

	@Autowired
	private ContextMulticastGroup contextMulticastGroup;

	@Autowired
	private ClusterId clusterId;

	@Override
	public void onJoin(String instanceId) {
		int channels = contextMulticastGroup.countOfChannel();
		if (channels >= 3) {
			log.info("Start leader election ...");
			context.propose("Leader", clusterId.get());
		}
	}

}
