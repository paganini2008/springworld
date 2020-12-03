package com.github.paganini2008.xtransport;

/**
 * 
 * TransportNodeCentre
 *
 * @author Jimmy Hoff
 * @since 1.0
 */
public interface TransportNodeCentre {

	void registerNode(Object attachment);

	Object findNode(String instanceId);

}
