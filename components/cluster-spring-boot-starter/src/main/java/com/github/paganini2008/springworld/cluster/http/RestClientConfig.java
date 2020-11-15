package com.github.paganini2008.springworld.cluster.http;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import com.github.paganini2008.springworld.cluster.ApplicationClusterAware;
import com.github.paganini2008.springworld.cluster.LeaderRecoveryCallback;
import com.github.paganini2008.springworld.cluster.multicast.ClusterMulticastConfig;

/**
 * 
 * RestClientConfig
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Configuration
@ConditionalOnBean(ClusterMulticastConfig.class)
public class RestClientConfig {

	@Value("${spring.application.cluster.name}")
	private String clusterName;

	@Bean
	public RegistryCenter applicationRegistryCenter() {
		return new ApplicationRegistryCenter();
	}

	@Bean
	public ApplicationClusterLoadBalancer applicationClusterLoadBalancer(RedisConnectionFactory connectionFactory) {
		final String name = ApplicationClusterAware.APPLICATION_CLUSTER_NAMESPACE + clusterName + ":counter";
		return new ApplicationClusterLoadBalancer(name, connectionFactory);
	}

	@ConditionalOnMissingBean
	@Bean
	public RoutingPolicy routingPolicy() {
		return new LoadBalanceRoutingPolicy();
	}

	@Bean
	public EnhancedRestTemplate enhancedRestTemplate() {
		return new EnhancedRestTemplate();
	}

	@Bean
	public RequestInterceptorContainer requestInterceptorContainer() {
		return new RequestInterceptorContainer();
	}

	@Bean
	public RetryTemplateFactory retryTemplateFactory() {
		return new RetryTemplateFactory();
	}

	@Bean
	public ApiRetryListenerContainer apiRetryListenerContainer() {
		return new ApiRetryListenerContainer();
	}

	@Bean
	public LoggingRetryListener loggingRetryListener() {
		return new LoggingRetryListener();
	}

	/**
	 * 
	 * LeaderHeartbeaterConfig
	 * 
	 * @author Fred Feng
	 *
	 * @since 1.0
	 */
	@EnableRestClient(include = { LeaderService.class })
	@Configuration
	public static class LeaderHeartbeaterConfig {

		@Bean
		public LeaderHeartbeater leaderHeartbeater() {
			return new LeaderHeartbeater();
		}
		
		@Primary
		@Bean
		public LeaderRecoveryCallback leaderRecoveryCallback() {
			return new RetryableLeaderRecoveryCallback();
		}
		
	}

}