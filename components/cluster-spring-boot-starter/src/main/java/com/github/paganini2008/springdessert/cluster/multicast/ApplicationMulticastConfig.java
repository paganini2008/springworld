package com.github.paganini2008.springdessert.cluster.multicast;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import com.github.paganini2008.springdessert.cluster.ApplicationClusterContext;
import com.github.paganini2008.springdessert.cluster.ApplicationClusterController;
import com.github.paganini2008.springdessert.cluster.ApplicationClusterLoadBalancer;
import com.github.paganini2008.springdessert.cluster.Constants;
import com.github.paganini2008.springdessert.cluster.DefaultInstanceIdGenerator;
import com.github.paganini2008.springdessert.cluster.InstanceId;
import com.github.paganini2008.springdessert.cluster.InstanceIdGenerator;
import com.github.paganini2008.springdessert.cluster.LoadBalancer;
import com.github.paganini2008.springdessert.cluster.RedisConnectionFailureHandler;
import com.github.paganini2008.springdessert.cluster.consistency.ConsistencyRequestConfig;
import com.github.paganini2008.springdessert.cluster.http.RestClientConfig;
import com.github.paganini2008.springdessert.cluster.pool.ProcessPoolConfig;

/**
 * 
 * ApplicationMulticastConfig
 * 
 * @author Jimmy Hoff
 *
 * @since 1.0
 */
@Configuration
@AutoConfigureBefore({ RestClientConfig.class, ProcessPoolConfig.class, ConsistencyRequestConfig.class })
@Import({ ApplicationMulticastController.class, ApplicationClusterController.class, RestClientConfig.class, ProcessPoolConfig.class,
		ConsistencyRequestConfig.class })
public class ApplicationMulticastConfig {

	@Value("${spring.application.cluster.name}")
	private String clusterName;

	@Bean
	@ConditionalOnMissingBean
	public InstanceIdGenerator defaultInstanceIdGenerator() {
		return new DefaultInstanceIdGenerator();
	}

	@Bean
	public InstanceId instanceId() {
		return new InstanceId();
	}

	@Bean
	public ApplicationClusterContext applicationClusterContext() {
		return new ApplicationClusterContext();
	}

	@Bean
	public ApplicationClusterListenerContainer applicationClusterListenerContainer() {
		return new ApplicationClusterListenerContainer();
	}

	@Bean
	public RedisConnectionFailureHandler redisConnectionFailureHandler() {
		return new RedisConnectionFailureHandler();
	}

	@Bean
	public ApplicationMulticastStarter applicationMulticastStarter() {
		return new ApplicationMulticastStarter();
	}

	@Bean
	public ApplicationMulticastStarterListener applicationMulticastStarterListener() {
		return new ApplicationMulticastStarterListener();
	}

	@Bean
	public ApplicationMessageStarterListener applicationMessageListener() {
		return new ApplicationMessageStarterListener();
	}

	@Bean
	public ApplicationClusterHeartbeatListener applicationClusterHeartbeatListener() {
		return new ApplicationClusterHeartbeatListener();
	}

	@Bean
	public ApplicationMulticastGroup applicationMulticastGroup() {
		return new ApplicationMulticastGroup();
	}

	@Bean
	public LoadBalancer multicastLoadBalancer(RedisConnectionFactory connectionFactory) {
		final String name = Constants.APPLICATION_CLUSTER_NAMESPACE + clusterName + ":counter:multicast";
		return new ApplicationClusterLoadBalancer(name, connectionFactory);
	}

	@Bean
	public MulticastMessageAcker multicastMessageAcker() {
		return new MulticastMessageAcker();
	}

	@Bean
	public LoggingApplicationMulticastListener loggingApplicationMulticastListener() {
		return new LoggingApplicationMulticastListener();
	}

	@Bean
	public ApplicationRegistryCenter applicationRegistryCenter() {
		return new ApplicationRegistryCenter();
	}

}
