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

	static final String ALL = "*";

	static final String LEADER = "L";

	String allocateHost(String provider, String path);

}
