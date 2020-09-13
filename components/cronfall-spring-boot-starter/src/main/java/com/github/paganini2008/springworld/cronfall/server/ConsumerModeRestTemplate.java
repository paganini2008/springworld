package com.github.paganini2008.springworld.cronfall.server;

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

	@Value("${spring.application.cluster.scheduler.applicationContextPaths}")
	private String contextPaths;

	@Override
	protected String[] getClusterApplicationContextPaths(String clusterName) {
		return this.contextPaths.split(",");
	}

}
