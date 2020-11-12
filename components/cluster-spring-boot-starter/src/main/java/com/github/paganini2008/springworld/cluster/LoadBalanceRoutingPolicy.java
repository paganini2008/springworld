package com.github.paganini2008.springworld.cluster;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.github.paganini2008.springworld.cluster.http.RoutingPolicy;
import com.github.paganini2008.springworld.cluster.http.RoutingPolicyException;

/**
 * 
 * LoadBalanceRoutingPolicy
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class LoadBalanceRoutingPolicy implements RoutingPolicy {

	private static final String LEADER_ALIAS = "*";

	@Autowired
	private ApplicationRegistryCenter applicationRegistryCenter;

	@Autowired
	private ApplicationClusterLoadBalancer loadBalancer;

	@Override
	public String extractUrl(String provider, String path) {
		ApplicationInfo selectedApplication;
		if (provider.equals(LEADER_ALIAS)) {
			selectedApplication = applicationRegistryCenter.getLeaderInfo();
		} else {
			List<ApplicationInfo> candidates = applicationRegistryCenter.getApplicationInfos(provider.toLowerCase());
			selectedApplication = loadBalancer.select(path, candidates);
		}
		if (selectedApplication == null) {
			throw new RoutingPolicyException("Invalid provider name: " + provider);
		}
		return selectedApplication.getApplicationContextPath() + path;
	}

}
