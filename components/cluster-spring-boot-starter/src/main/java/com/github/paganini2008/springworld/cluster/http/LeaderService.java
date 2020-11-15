package com.github.paganini2008.springworld.cluster.http;

import org.springframework.http.HttpMethod;

import com.github.paganini2008.springworld.cluster.ApplicationInfo;

/**
 * 
 * LeaderService
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@RestClient(provider = "*", retries = 2)
public interface LeaderService {

	@Api(path = "/application/cluster/ping", method = HttpMethod.GET)
	ApplicationInfo ping();

}
