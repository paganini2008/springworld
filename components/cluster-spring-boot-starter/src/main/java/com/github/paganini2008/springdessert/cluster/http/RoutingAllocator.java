package com.github.paganini2008.springdessert.cluster.http;

/**
 * 
 * RoutingAllocator
 * 
 * @author Jimmy Hoff
 *
 * @since 1.0
 */
public interface RoutingAllocator {

	static final String LEADER_ALIAS = "*";

	String allocateHost(String provider, String path);

}
