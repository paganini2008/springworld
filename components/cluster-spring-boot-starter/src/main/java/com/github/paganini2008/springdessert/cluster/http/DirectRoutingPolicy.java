package com.github.paganini2008.springdessert.cluster.http;

/**
 * 
 * DirectRoutingPolicy
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class DirectRoutingPolicy implements RoutingPolicy {

	@Override
	public String extractUrl(String provider, String path) {
		return provider + path;
	}

}