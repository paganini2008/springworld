package com.github.paganini2008.springdessert.cluster.multicast;

import com.github.paganini2008.springdessert.cluster.ApplicationInfo;

/**
 * 
 * ApplicationMulticastListener
 * 
 * @author Jimmy Hoff
 *
 * @since 1.0
 */
public interface ApplicationMulticastListener extends ApplicationClusterListener {

	default void onActive(ApplicationInfo applicationInfo) {
	}

	default void onInactive(ApplicationInfo applicationInfo) {
	}

	default void onGlobalMessage(ApplicationInfo applicationInfo, String id, Object message) {
	}

}
