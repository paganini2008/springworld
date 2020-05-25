package com.github.paganini2008.springworld.cluster.multicast;

/**
 * 
 * ClusterMessageListener
 *
 * @author Fred Feng
 * @since 1.0
 */
public interface ClusterMessageListener extends ClusterMulticastListener {

	void onMessage(String anotherInstanceId, Object message);

	default String getTopic() {
		return "*";
	}

}
