package com.github.paganini2008.springworld.crontab.server;

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
	protected String[] getClusterApplicationContextPaths(String clusterName) {
		return clusterRegistry.getClusterApplicationContextPaths(clusterName);
	}

}
