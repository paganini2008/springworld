package com.github.paganini2008.springworld.cluster.multicast;

/**
 * 
 * ClusterStateChangeListener
 * 
 * @author Fred Feng
 * @version 1.0
 */
public interface ClusterStateChangeListener extends ClusterMulticastListener {

	default void onActive(String anotherInstanceId) {
	}

	default void onInactive(String anotherInstanceId) {
	}

	default void onMessage(String anotherInstanceId, Object message) {
	}

}
