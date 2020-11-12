package com.github.paganini2008.springworld.cluster.http;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import com.github.paganini2008.springworld.cluster.ApplicationClusterAware;
import com.github.paganini2008.springworld.cluster.ApplicationClusterLoadBalancer;
import com.github.paganini2008.springworld.cluster.ApplicationRegistryCenter;
import com.github.paganini2008.springworld.cluster.LoadBalanceRoutingPolicy;

/**
 * 
 * RestClientConfig
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Configuration
public class RestClientConfig {

	@Value("${spring.application.cluster.name}")
	private String clusterName;

	@Bean
	public ApplicationRegistryCenter applicationRegistryCenter() {
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
	public DefaultRetryListener defaultRetryListener() {
		return new DefaultRetryListener();
	}

}
