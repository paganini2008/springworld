package com.github.paganini2008.springdessert.cluster.monitor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health.Builder;

import com.github.paganini2008.devtools.io.FileUtils;
import com.github.paganini2008.springdessert.cluster.ApplicationClusterContext;
import com.github.paganini2008.springdessert.cluster.ApplicationInfo;
import com.github.paganini2008.springdessert.cluster.HealthState;
import com.github.paganini2008.springdessert.cluster.multicast.ApplicationMulticastGroup;

/**
 * 
 * ApplicationClusterHealthIndicator
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
public class ApplicationClusterHealthIndicator extends AbstractHealthIndicator {

	@Autowired
	private ApplicationClusterContext applicationClusterContext;

	@Autowired
	private ApplicationMulticastGroup applicationMulticastGroup;

	@Override
	protected void doHealthCheck(Builder builder) throws Exception {
		HealthState healthState = applicationClusterContext.getHealthState();
		if (healthState == HealthState.FATAL) {
			builder.down();
		} else {
			builder.up();
		}
		builder.withDetail("healthState", healthState);
		builder.withDetail("totalMemory", FileUtils.formatSize(Runtime.getRuntime().totalMemory()));
		builder.withDetail("usedMemory", FileUtils.formatSize(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
		builder.withDetail("candidates", applicationMulticastGroup.countOfCandidate());
		builder.withDetail("leader", getLeaderInfo());
	}

	private ApplicationInfo getLeaderInfo() {
		for (ApplicationInfo candidate : applicationMulticastGroup.getCandidates()) {
			if (candidate.isLeader()) {
				return candidate;
			}
		}
		return null;
	}

}
