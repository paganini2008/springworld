package com.github.paganini2008.springworld.jobstorm.ui;

import org.springframework.beans.factory.annotation.Value;

import com.github.paganini2008.springworld.jobstorm.server.ClusterRestTemplate;

/**
 * 
 * UIModeClusterRestTemplate
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class UIModeClusterRestTemplate extends ClusterRestTemplate {

	@Value("${jobstorm.cluster.contextPaths}")
	private String contextPaths;

	@Override
	protected String[] getClusterContextPaths(String clusterName) {
		return this.contextPaths.split(",");
	}

}
