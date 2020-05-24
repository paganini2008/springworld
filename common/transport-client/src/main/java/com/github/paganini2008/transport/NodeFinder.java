package com.github.paganini2008.transport;

/**
 * 
 * NodeFinder
 *
 * @author Fred Feng
 * @since 1.0
 */
public interface NodeFinder {

	default void registerNode(Object attachment) {
		throw new UnsupportedOperationException("Only being used by spring application context.");
	}

	Object findNode(String instanceId);

	default void destroy() {
	}

}
