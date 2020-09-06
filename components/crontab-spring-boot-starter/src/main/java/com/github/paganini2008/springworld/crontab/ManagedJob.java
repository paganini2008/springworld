package com.github.paganini2008.springworld.crontab;

import org.springframework.beans.factory.annotation.Value;

/**
 * 
 * ManagedJob
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public abstract class ManagedJob implements Job {

	@Value("${spring.application.cluster.name}")
	private String clusterName;

	@Value("${spring.application.name}")
	private String applicationName;

	@Override
	public String getClusterName() {
		return clusterName;
	}

	@Override
	public String getGroupName() {
		return applicationName;
	}
}
