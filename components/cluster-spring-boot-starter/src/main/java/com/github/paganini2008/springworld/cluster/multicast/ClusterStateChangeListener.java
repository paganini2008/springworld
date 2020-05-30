package com.github.paganini2008.springworld.cluster.multicast;

import com.github.paganini2008.springworld.cluster.ApplicationInfo;

/**
 * 
 * ClusterStateChangeListener
 * 
 * @author Fred Feng
 * @version 1.0
 */
public interface ClusterStateChangeListener extends ClusterMulticastListener {

	default void onActive(ApplicationInfo applicationInfo) {
	}

	default void onInactive(ApplicationInfo applicationInfo) {
	}

	default void onMessage(ApplicationInfo applicationInfo, Object message) {
	}

}
