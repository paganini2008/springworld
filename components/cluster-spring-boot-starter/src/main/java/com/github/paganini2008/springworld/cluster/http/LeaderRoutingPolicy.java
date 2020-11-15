package com.github.paganini2008.springworld.cluster.http;

import org.springframework.beans.factory.annotation.Autowired;

import com.github.paganini2008.springworld.cluster.ApplicationInfo;

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
	private RegistryCenter registryCenter;

	@Override
	public String extractUrl(String provider, String path) {
		ApplicationInfo leaderInfo = registryCenter.getLeader();
		return leaderInfo.getApplicationContextPath() + path;
	}

}
