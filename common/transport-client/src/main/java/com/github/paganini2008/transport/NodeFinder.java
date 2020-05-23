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
		throw new UnsupportedOperationException("Only used by spring context.");
	}

	Object findNode(String instanceId);

	default void destroy() {
	}

}
