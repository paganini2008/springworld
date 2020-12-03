package com.github.paganini2008.springdessert.cluster.http;

import org.springframework.http.HttpMethod;

import com.github.paganini2008.springdessert.cluster.ApplicationInfo;
import com.github.paganini2008.springdessert.cluster.ClusterState;

/**
 * 
 * LeaderService
 * 
 * @author Jimmy Hoff
 *
 * @since 1.0
 */
@RestClient(provider = RoutingPolicy.LEADER_ALIAS)
public interface LeaderService {

	@Api(path = "/application/cluster/ping", method = HttpMethod.GET, retries = 2, timeout = 60)
	ApplicationInfo ping();

	@Api(path = "/application/cluster/state", method = HttpMethod.GET, retries = 2, timeout = 60)
	ClusterState state();

	@Api(path = "/application/cluster/list", method = HttpMethod.GET)
	ApplicationInfo[] list();

	@Api(path = "/application/cluster/recovery", method = HttpMethod.GET)
	ApplicationInfo[] recovery();

}
