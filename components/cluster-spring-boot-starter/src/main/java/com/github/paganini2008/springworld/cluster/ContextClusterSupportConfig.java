package com.github.paganini2008.springworld.cluster;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 
 * ContextClusterSupportConfig
 *
 * @author Fred Feng
 * @version 1.0
 */
@Configuration
public class ContextClusterSupportConfig {

	@Bean
	@ConditionalOnMissingBean(ClusterIdGenerator.class)
	public ClusterIdGenerator clusterIdGenerator() {
		return new DefaultClusterIdGenerator();
	}

	@Bean
	public ClusterId clusterId() {
		return new ClusterId();
	}

}
