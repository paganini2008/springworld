package com.github.paganini2008.springworld.cluster;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.github.paganini2008.springworld.restclient.RoutingPolicy;

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

	@Qualifier("applicationClusterLoadBalancer")
	@Autowired
	private LoadBalancer<ApplicationInfo> loadBalancer;

	@Override
	public String extractUrl(String provider, String path) {
		ApplicationInfo selectedApplication;
		if (provider.equals(LEADER_ALIAS)) {
			selectedApplication = applicationRegistryCenter.getLeaderInfo();
		} else {
			List<ApplicationInfo> candidates = applicationRegistryCenter.getApplicationInfos(provider);
			selectedApplication = loadBalancer.select(path, candidates);
		}
		return selectedApplication.getApplicationContextPath() + path;
	}

}
