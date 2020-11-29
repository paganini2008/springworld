package com.github.paganini2008.springdessert.cluster.multicast;

import com.github.paganini2008.springdessert.cluster.ApplicationInfo;

/**
 * 
 * MulticastGroupListener
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public interface MulticastGroupListener extends MulticastListener {

	default void onActive(ApplicationInfo applicationInfo) {
	}

	default void onInactive(ApplicationInfo applicationInfo) {
	}

	default void onMessage(ApplicationInfo applicationInfo, Object message) {
	}

}
