package com.github.paganini2008.springworld.cluster;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import com.github.paganini2008.springworld.restclient.RoutingPolicy;

/**
 * 
 * ApplicationClusterSupportConfig
 *
 * @author Fred Feng
 * @version 1.0
 */
@Configuration
public class ApplicationClusterSupportConfig {

	@Value("${spring.application.cluster.name:default}")
	private String clusterName;

	@Bean
	@ConditionalOnMissingBean(InstanceIdGenerator.class)
	public InstanceIdGenerator instanceIdGenerator() {
		return new DefaultInstanceIdGenerator();
	}

	@Bean
	public InstanceId instanceId() {
		return new InstanceId();
	}
	
	@Bean
	public ApplicationRegistryCenter applicationRegistryCenter() {
		return new ApplicationRegistryCenter();
	}

	@Bean
	public LoadBalancer<ApplicationInfo> applicationClusterLoadBalancer(RedisConnectionFactory connectionFactory) {
		final String name = ApplicationClusterAware.APPLICATION_CLUSTER_NAMESPACE + clusterName + ":counter";
		return new ApplicationClusterLoadBalancer(name, connectionFactory);
	}

	@Bean
	public RoutingPolicy routingPolicy() {
		return new LoadBalanceRoutingPolicy();
	}

}
