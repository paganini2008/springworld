package com.github.paganini2008.springworld.cluster.http;

/**
 * 
 * RoutingPolicy
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public interface RoutingPolicy {

	String extractUrl(String provider, String path);

}
