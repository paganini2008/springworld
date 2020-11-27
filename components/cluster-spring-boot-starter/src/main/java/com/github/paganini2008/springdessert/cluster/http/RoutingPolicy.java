package com.github.paganini2008.springdessert.cluster.http;

/**
 * 
 * RoutingPolicy
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public interface RoutingPolicy {

	static final String LEADER_ALIAS = "*";

	String extractUrl(String provider, String path);

}
