package com.github.paganini2008.springdessert.jobsoup.server;

import org.springframework.beans.factory.annotation.Value;

/**
 * 
 * ConsumerModeRestTemplate
 * 
 * @author Jimmy Hoff
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
