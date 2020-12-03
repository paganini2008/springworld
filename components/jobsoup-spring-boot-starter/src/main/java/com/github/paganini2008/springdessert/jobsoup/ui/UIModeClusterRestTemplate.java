package com.github.paganini2008.springdessert.jobsoup.ui;

import org.springframework.beans.factory.annotation.Value;

import com.github.paganini2008.springdessert.jobsoup.server.ClusterRestTemplate;

/**
 * 
 * UIModeClusterRestTemplate
 * 
 * @author Jimmy Hoff
 *
 * @since 1.0
 */
public class UIModeClusterRestTemplate extends ClusterRestTemplate {

	@Value("${jobsoup.server.mode.producer.location}")
	private String contextPaths;

	@Override
	protected String[] getClusterContextPaths(String clusterName) {
		return this.contextPaths.split(",");
	}

}
