package com.github.paganini2008.springdessert.jobsoup.server;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * ProducerModeRestTemplate
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class ProducerModeRestTemplate extends ClusterRestTemplate {

	@Autowired
	private ClusterRegistry clusterRegistry;

	@Override
	protected String[] getClusterContextPaths(String clusterName) {
		return clusterRegistry.getClusterContextPaths(clusterName);
	}

}
