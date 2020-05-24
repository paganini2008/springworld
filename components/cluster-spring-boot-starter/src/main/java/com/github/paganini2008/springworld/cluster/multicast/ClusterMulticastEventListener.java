package com.github.paganini2008.springworld.cluster.multicast;

import java.util.EventListener;

/**
 * 
 * ClusterMulticastEventListener
 * 
 * @author Fred Feng
 * @version 1.0
 */
public interface ClusterMulticastEventListener extends EventListener {

	default void onActive(String instanceId) {
	}

	default void onInactive(String instanceId) {
	}

	default void onMessage(String instanceId, Object message) {
	}

	default void onGlobalMessage(String instanceId, Object message) {
	}

	default String getTopic() {
		return ClusterMulticastEventListenerContainer.GLOBAL_TOPIC;
	}

}
