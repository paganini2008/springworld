package com.github.paganini2008.springworld.cluster;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 
 * ContextClusterConfig
 *
 * @author Fred Feng
 * @version 1.0
 */
@Configuration
@ConditionalOnProperty(value = "spring.application.cluster.enabled", havingValue = "true", matchIfMissing = true)
public class ContextClusterConfig {

	@Bean(name = "clusterHeartbeatThread", destroyMethod = "stop")
	public ContextClusterHeartbeatThread heartbeatThread() {
		return new ContextClusterHeartbeatThread();
	}

	@Bean
	public ContextClusterAware contextClusterAware() {
		return new ContextClusterAware();
	}

	@Bean
	public ContextMasterBreakdownListener masterBreakdownListener() {
		return new ContextMasterBreakdownListener();
	}
}
