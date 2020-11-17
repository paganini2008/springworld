package com.github.paganini2008.springworld.jobsoup.server;

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

	@Value("${jobsoup.server.mode.producer.location}")
	private String contextPaths;

	@Override
	protected String[] getClusterContextPaths(String clusterName) {
		return this.contextPaths.split(",");
	}

}
