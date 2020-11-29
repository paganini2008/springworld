package com.github.paganini2008.springdessert.cluster.http;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.github.paganini2008.springdessert.cluster.ApplicationClusterLoadBalancer;
import com.github.paganini2008.springdessert.cluster.ApplicationInfo;

/**
 * 
 * LoadBalanceRoutingPolicy
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class LoadBalanceRoutingPolicy implements RoutingPolicy {

	@Autowired
	private RegistryCenter registryCenter;

	@Autowired
	private ApplicationClusterLoadBalancer loadBalancer;

	@Override
	public String extractUrl(String provider, String path) {
		ApplicationInfo selectedApplication;
		if (provider.equals(LEADER_ALIAS)) {
			selectedApplication = registryCenter.getLeader();
		} else {
			List<ApplicationInfo> candidates = registryCenter.getApplications(provider.toLowerCase());
			selectedApplication = loadBalancer.select(path, candidates);
		}
		if (selectedApplication == null) {
			throw new RoutingPolicyException("Invalid provider name: " + provider);
		}
		return selectedApplication.getApplicationContextPath() + path;
	}

}
