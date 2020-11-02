package com.github.paganini2008.springworld.jobsoup.ui;

import org.springframework.beans.factory.annotation.Value;

import com.github.paganini2008.springworld.jobsoup.server.ClusterRestTemplate;

/**
 * 
 * UIModeClusterRestTemplate
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class UIModeClusterRestTemplate extends ClusterRestTemplate {

	@Value("${jobsoup.cluster.contextPaths}")
	private String contextPaths;

	@Override
	protected String[] getClusterContextPaths(String clusterName) {
		return this.contextPaths.split(",");
	}

}
