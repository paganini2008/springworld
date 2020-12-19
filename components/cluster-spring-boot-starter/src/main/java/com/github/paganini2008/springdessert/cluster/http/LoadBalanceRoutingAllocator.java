package com.github.paganini2008.springdessert.cluster.http;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationListener;

import com.github.paganini2008.springdessert.cluster.ApplicationInfo;
import com.github.paganini2008.springdessert.cluster.LoadBalancer;
import com.github.paganini2008.springdessert.cluster.election.ApplicationClusterFollowerEvent;
import com.github.paganini2008.springdessert.cluster.election.LeaderNotFoundException;

/**
 * 
 * LoadBalanceRoutingAllocator
 *
 * @author Jimmy Hoff
 * 
 * @since 1.0
 */
public class LoadBalanceRoutingAllocator implements RoutingAllocator, ApplicationListener<ApplicationClusterFollowerEvent> {

	@Autowired
	private RegistryCenter registryCenter;

	@Qualifier("applicationClusterLoadBalancer")
	@Autowired
	private LoadBalancer loadBalancer;

	private ApplicationInfo leaderInfo;

	@Override
	public String allocateHost(String provider, String path) {
		ApplicationInfo selectedApplication;
		if (provider.equals(LEADER_ALIAS)) {
			if (leaderInfo == null) {
				throw new LeaderNotFoundException();
			}
			selectedApplication = leaderInfo;
		} else {
			List<ApplicationInfo> candidates = registryCenter.getApplications(provider.toLowerCase());
			selectedApplication = loadBalancer.select(path, candidates);
		}
		if (selectedApplication == null) {
			throw new RoutingPolicyException("Invalid provider name: " + provider);
		}
		return selectedApplication.getApplicationContextPath() + path;
	}

	@Override
	public void onApplicationEvent(ApplicationClusterFollowerEvent event) {
		this.leaderInfo = event.getLeaderInfo();
	}

}
