package com.github.paganini2008.springdessert.cluster.http;

import org.springframework.context.ApplicationListener;

import com.github.paganini2008.springdessert.cluster.ApplicationInfo;
import com.github.paganini2008.springdessert.cluster.election.ApplicationClusterFollowerEvent;
import com.github.paganini2008.springdessert.cluster.election.LeaderNotFoundException;

/**
 * 
 * LeaderRoutingAllocator
 * 
 * @author Jimmy Hoff
 *
 * @since 1.0
 */
public class LeaderRoutingAllocator implements RoutingAllocator, ApplicationListener<ApplicationClusterFollowerEvent> {

	private ApplicationInfo leaderInfo;

	@Override
	public void onApplicationEvent(ApplicationClusterFollowerEvent event) {
		this.leaderInfo = event.getLeaderInfo();
	}

	@Override
	public String allocateHost(String provider, String path) {
		if (leaderInfo == null) {
			throw new LeaderNotFoundException();
		}
		return leaderInfo.getApplicationContextPath() + path;
	}

}
