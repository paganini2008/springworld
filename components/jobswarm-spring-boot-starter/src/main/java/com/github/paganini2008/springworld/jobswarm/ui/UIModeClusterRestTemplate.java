package com.github.paganini2008.springworld.jobswarm.ui;

import org.springframework.beans.factory.annotation.Value;

import com.github.paganini2008.springworld.jobswarm.server.ClusterRestTemplate;

/**
 * 
 * UIModeClusterRestTemplate
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class UIModeClusterRestTemplate extends ClusterRestTemplate {

	@Value("${spring.application.cluster.scheduler.applicationContextPaths}")
	private String contextPaths;

	@Override
	protected String[] getClusterApplicationContextPaths(String clusterName) {
		return this.contextPaths.split(",");
	}

}
