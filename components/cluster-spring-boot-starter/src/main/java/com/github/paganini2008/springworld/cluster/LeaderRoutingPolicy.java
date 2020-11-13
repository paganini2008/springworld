package com.github.paganini2008.springworld.cluster;

import org.springframework.beans.factory.annotation.Autowired;

import com.github.paganini2008.springworld.cluster.http.RoutingPolicy;

/**
 * 
 * LeaderRoutingPolicy
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class LeaderRoutingPolicy implements RoutingPolicy {

	@Autowired
	private ApplicationRegistryCenter applicationRegistryCenter;

	@Override
	public String extractUrl(String provider, String path) {
		ApplicationInfo leaderInfo = applicationRegistryCenter.getLeaderInfo();
		return leaderInfo.getApplicationContextPath() + path;
	}

}
