package com.github.paganini2008.springworld.cronfall.ui;

import org.springframework.beans.factory.annotation.Autowired;

import com.github.paganini2008.springworld.cronfall.server.ClusterRegistry;
import com.github.paganini2008.springworld.cronfall.server.ClusterRestTemplate;

/**
 * 
 * UIModeClusterRestTemplate
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class UIModeClusterRestTemplate extends ClusterRestTemplate {

	@Autowired
	private ClusterRegistry clusterRegistry;

	@Override
	protected String[] getClusterApplicationContextPaths(String clusterName) {
		return clusterRegistry.getClusterApplicationContextPaths(clusterName);
	}

}
