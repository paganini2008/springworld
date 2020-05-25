package com.github.paganini2008.springworld.cluster.multicast;

/**
 * 
 * ClusterStateChangeListener
 * 
 * @author Fred Feng
 * @version 1.0
 */
public interface ClusterStateChangeListener extends ClusterMulticastListener {

	default void onActive(String instanceId) {
	}

	default void onInactive(String instanceId) {
	}

	default void onMessage(String instanceId, Object message) {
	}

}
