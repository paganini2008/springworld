package com.github.paganini2008.springworld.cluster;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 
 * ApplicationClusterConfig
 *
 * @author Fred Feng
 * @version 1.0
 */
@Configuration
@ConditionalOnProperty(value = "spring.application.cluster.enabled", havingValue = "true", matchIfMissing = true)
public class ApplicationClusterConfig {

	@Bean(name = "clusterHeartbeatThread", destroyMethod = "stop")
	public ApplicationClusterHeartbeatThread clusterHeartbeatThread() {
		return new ApplicationClusterHeartbeatThread();
	}

	@Bean
	public ApplicationClusterAware clusterAware() {
		return new ApplicationClusterAware();
	}

	@Bean
	public ApplicationClusterLeaderMissingListener clusterLeaderMissingListener() {
		return new ApplicationClusterLeaderMissingListener();
	}
}