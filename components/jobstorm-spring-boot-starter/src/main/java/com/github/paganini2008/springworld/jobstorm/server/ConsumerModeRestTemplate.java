package com.github.paganini2008.springworld.jobstorm.server;

import org.springframework.beans.factory.annotation.Value;

/**
 * 
 * ConsumerModeRestTemplate
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class ConsumerModeRestTemplate extends ClusterRestTemplate {

	@Value("${jobstorm.cluster.contextPaths}")
	private String contextPaths;

	@Override
	protected String[] getClusterContextPaths(String clusterName) {
		return this.contextPaths.split(",");
	}

}
