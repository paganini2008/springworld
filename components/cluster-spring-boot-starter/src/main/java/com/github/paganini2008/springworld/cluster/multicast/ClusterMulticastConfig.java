package com.github.paganini2008.springworld.cluster.multicast;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import com.github.paganini2008.springworld.cluster.ApplicationClusterAware;
import com.github.paganini2008.springworld.reditools.messager.RedisMessageHandler;

/**
 * 
 * ClusterMulticastConfig
 *
 * @author Fred Feng
 * @version 1.0
 */
@Configuration
@ConditionalOnProperty(value = "spring.application.cluster.multicast.enabled", havingValue = "true")
public class ClusterMulticastConfig {

	@Value("${spring.application.cluster.name:default}")
	private String clusterName;

	@Bean
	public ClusterMulticastAware clusterMulticastAware() {
		return new ClusterMulticastAware();
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
	public ClusterMulticastGroup multicastGroup() {
		return new ClusterMulticastGroup();
	}

	@Bean
	@ConditionalOnMissingBean(LoadBalance.class)
	public LoadBalance loadBalance(RedisConnectionFactory connectionFactory) {
		final String key = ApplicationClusterAware.APPLICATION_CLUSTER_NAMESPACE + clusterName + ":counter";
		return new CachedRoundRobinLoadBalance(key, connectionFactory);
	}

	@Bean(name = "multicastHeartbeatThread", initMethod = "start", destroyMethod = "stop")
	public ClusterMulticastHeartbeatThread multicastHeartbeatThread() {
		return new ClusterMulticastHeartbeatThread();
	}

	@Bean
	public ClusterMulticastMessageAcker clusterMulticastMessageAcker() {
		return new ClusterMulticastMessageAcker();
	}

	@Bean
	public ClusterMulticastListenerContainer multicastListenerContainer() {
		return new ClusterMulticastListenerContainer();
	}

	@Bean
	public ClusterStateChangeListener loggingMulticastEventListener() {
		return new LoggingClusterMulticastEventListener();
	}

}
