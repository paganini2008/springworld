package com.github.paganini2008.springworld.cluster.multicast;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * LoggingClusterMulticastEventListener
 * 
 * @author Fred Feng
 * @version 1.0
 */
@Slf4j
public class LoggingClusterMulticastEventListener implements ClusterStateChangeListener {

	@Override
	public void onActive(String clusterId) {
		if (log.isTraceEnabled()) {
			log.trace("Spring application '{}' has joined.", clusterId);
		}
	}

	@Override
	public void onInactive(String clusterId) {
		if (log.isTraceEnabled()) {
			log.trace("Spring application '{}' has gone.", clusterId);
		}
	}

	@Override
	public void onMessage(String clusterId, Object message) {
		if (log.isTraceEnabled()) {
			log.trace("Spring application '{}' send message: {}", clusterId, message);
		}
	}

}
