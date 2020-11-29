package com.github.paganini2008.transport;

/**
 * 
 * TransportNodeCentre
 *
 * @author Fred Feng
 * @since 1.0
 */
public interface TransportNodeCentre {

	void registerNode(Object attachment);

	Object findNode(String instanceId);

}
