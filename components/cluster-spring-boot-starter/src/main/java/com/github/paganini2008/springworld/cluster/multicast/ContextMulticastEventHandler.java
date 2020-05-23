package com.github.paganini2008.springworld.cluster.multicast;

/**
 * 
 * ContextMulticastEventHandler
 * 
 * @author Fred Feng
 * @version 1.0
 */
public interface ContextMulticastEventHandler {

	default void onJoin(String instanceId) {
	}

	default void onLeave(String instanceId) {
	}

	default void onMessage(String instanceId, Object message) {
	}

	default void onGlobalMessage(String instanceId, Object message) {
	}

	default String getTopic() {
		return ContextMulticastEventListener.GLOBAL_TOPIC;
	}

}
