package com.github.paganini2008.springdessert.cluster.http;

import org.springframework.http.HttpMethod;

import com.github.paganini2008.springdessert.cluster.ApplicationInfo;
import com.github.paganini2008.springdessert.cluster.HealthState;

/**
 * 
 * LeaderService
 * 
 * @author Jimmy Hoff
 *
 * @since 1.0
 */
@RestClient(provider = RoutingAllocator.LEADER_ALIAS)
public interface LeaderService {

	@Api(path = "/application/cluster/ping", method = HttpMethod.GET, retries = 3, timeout = 60)
	ApplicationInfo ping();

	@Api(path = "/application/cluster/state", method = HttpMethod.GET, retries = 3, timeout = 60)
	HealthState state();

	@Api(path = "/application/cluster/list", method = HttpMethod.GET)
	ApplicationInfo[] list();

	@Api(path = "/application/cluster/recovery", method = HttpMethod.GET)
	ApplicationInfo[] recovery();

}
