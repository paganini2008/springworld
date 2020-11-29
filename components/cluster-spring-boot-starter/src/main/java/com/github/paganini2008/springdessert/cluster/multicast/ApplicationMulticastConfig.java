package com.github.paganini2008.springdessert.cluster.multicast;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import com.github.paganini2008.springdessert.cluster.ApplicationClusterAware;
import com.github.paganini2008.springdessert.cluster.ApplicationClusterLoadBalancer;
import com.github.paganini2008.springdessert.cluster.utils.LoadBalancer;
import com.github.paganini2008.springdessert.reditools.messager.RedisMessageHandler;

/**
 * 
 * ApplicationMulticastConfig
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Configuration
@ConditionalOnProperty(value = "spring.application.cluster.multicast.enabled", havingValue = "true", matchIfMissing = true)
@Import({ ApplicationMulticastController.class })
public class ApplicationMulticastConfig {

	@Value("${spring.application.cluster.name}")
	private String clusterName;

	@Bean
	public ApplicationMulticastAware applicationMulticastAware() {
		return new ApplicationMulticastAware();
	}

	@Bean
	public RedisMessageHandler applicationActiveListener() {
		return new ApplicationActiveListener();
	}

	@Bean
	public RedisMessageHandler applicationInactiveListener() {
		return new ApplicationInactiveListener();
	}

	@Bean
	public RedisMessageHandler applicationMessageListener() {
		return new ApplicationMessageListener();
	}

	@Bean
	public ApplicationMulticastGroup applicationMulticastGroup() {
		return new ApplicationMulticastGroup();
	}

	@Bean
	public LoadBalancer multicastLoadBalancer(RedisConnectionFactory connectionFactory) {
		final String name = ApplicationClusterAware.APPLICATION_CLUSTER_NAMESPACE + clusterName + ":counter:multicast";
		return new ApplicationClusterLoadBalancer(name, connectionFactory);
	}

	@Bean
	public MulticastMessageAcker multicastMessageAcker() {
		return new MulticastMessageAcker();
	}

	@Bean
	public MulticastListenerContainer multicastListenerContainer() {
		return new MulticastListenerContainer();
	}

	@Bean
	public MulticastGroupListener loggingMulticastGroupListener() {
		return new LoggingMulticastGroupListener();
	}

}
