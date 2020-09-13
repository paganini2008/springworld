package com.github.paganini2008.springworld.cronkeeper.ui;

import org.springframework.beans.factory.annotation.Autowired;

import com.github.paganini2008.springworld.cronkeeper.server.ClusterRegistry;
import com.github.paganini2008.springworld.cronkeeper.server.ClusterRestTemplate;

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
