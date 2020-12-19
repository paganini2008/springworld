package com.github.paganini2008.springdessert.cluster.monitor;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.github.paganini2008.springdessert.cluster.http.RestClientConfig;
import com.github.paganini2008.springdessert.cluster.multicast.ApplicationMulticastConfig;

/**
 * 
 * HealthIndicatorConfig
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
@Configuration
public class HealthIndicatorConfig {

	@Bean("applicationClusterHealth")
	@ConditionalOnBean(ApplicationMulticastConfig.class)
	public ApplicationClusterHealthIndicator applicationClusterHealthIndicator() {
		return new ApplicationClusterHealthIndicator();
	}

	@Bean("taskExecutorHealth")
	@ConditionalOnBean(ThreadPoolTaskExecutor.class)
	public TaskExecutorHealthIndicator taskExecutorHealthIndicator() {
		return new TaskExecutorHealthIndicator();
	}

	@Bean("restClientHealth")
	@ConditionalOnBean(RestClientConfig.class)
	public RestClientHealthIndicator restClientHealthIndicator() {
		return new RestClientHealthIndicator();
	}

}
