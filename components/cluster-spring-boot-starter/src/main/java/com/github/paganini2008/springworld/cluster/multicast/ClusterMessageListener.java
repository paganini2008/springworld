package com.github.paganini2008.springworld.cluster.multicast;

import com.github.paganini2008.springworld.cluster.ApplicationInfo;

/**
 * 
 * ClusterMessageListener
 *
 * @author Fred Feng
 * @since 1.0
 */
public interface ClusterMessageListener extends ClusterMulticastListener {

	void onMessage(ApplicationInfo applicationInfo, String id, Object message);

	default String getTopic() {
		return "*";
	}

}
