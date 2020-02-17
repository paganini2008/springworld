package com.github.paganini2008.springworld.cluster;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;

/**
 * 
 * DefaultClusterIdGenerator
 * 
 * @author Fred Feng
 * 
 * 
 * @version 1.0
 */
public class DefaultClusterIdGenerator implements ClusterIdGenerator {

	@Value("${spring.application.name}")
	private String applicationName;

	@Override
	public String generateClusterId() {
		return applicationName + "@" + UUID.randomUUID().toString().replace("-", "");
	}

}
