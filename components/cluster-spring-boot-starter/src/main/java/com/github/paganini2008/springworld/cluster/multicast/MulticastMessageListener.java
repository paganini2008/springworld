package com.github.paganini2008.springworld.cluster.multicast;

import com.github.paganini2008.springworld.cluster.ApplicationInfo;

/**
 * 
 * MulticastMessageListener
 *
 * @author Fred Feng
 * @since 1.0
 */
public interface MulticastMessageListener extends MulticastListener {

	void onMessage(ApplicationInfo applicationInfo, String id, Object message);

	default String getTopic() {
		return "*";
	}

}
