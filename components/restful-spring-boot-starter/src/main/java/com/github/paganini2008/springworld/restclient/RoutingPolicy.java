package com.github.paganini2008.springworld.restclient;

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
