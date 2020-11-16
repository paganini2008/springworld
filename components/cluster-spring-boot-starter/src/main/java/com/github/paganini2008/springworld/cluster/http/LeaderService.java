package com.github.paganini2008.springworld.cluster.http;

import org.springframework.http.HttpMethod;

import com.github.paganini2008.springworld.cluster.ApplicationInfo;
import com.github.paganini2008.springworld.cluster.ClusterState;

/**
 * 
 * LeaderService
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@RestClient(provider = RoutingPolicy.LEADER_ALIAS)
public interface LeaderService {

	@Api(path = "/application/cluster/ping", method = HttpMethod.GET, retries = 3, timeout = 60)
	ApplicationInfo ping();

	@Api(path = "/application/cluster/state", method = HttpMethod.GET, retries = 3, timeout = 60)
	ClusterState state();

}
