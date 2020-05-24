package com.github.paganini2008.springworld.cluster.multicast;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.paganini2008.springworld.redis.pubsub.RedisMessageHandler;

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

	@Bean
	public ClusterMulticastAware clusterMulticastAware() {
		return new ClusterMulticastAware();
	}

	@Bean
	public RedisMessageHandler activeEventListener() {
		return new ApplicationActiveEventListener();
	}

	@Bean
	public RedisMessageHandler inactiveEventListener() {
		return new ApplicationInactiveEventListener();
	}

	@Bean
	public RedisMessageHandler messageEventListener() {
		return new ApplicationMessageEventListener();
	}

	@Bean
	public ClusterMulticastGroup multicastGroup() {
		return new ClusterMulticastGroup();
	}

	@Bean
	@ConditionalOnMissingBean(LoadBalance.class)
	public LoadBalance loadBalance() {
		return new LoadBalanceSelector.RoundrobinLoadBalance();
	}

	@Bean(name = "multicastHeartbeatThread", initMethod = "start", destroyMethod = "stop")
	public ClusterMulticastHeartbeatThread multicastHeartbeatThread() {
		return new ClusterMulticastHeartbeatThread();
	}

	@Bean
	public ClusterMulticastEventListenerContainer multicastEventListenerContainer() {
		return new ClusterMulticastEventListenerContainer();
	}

	@Bean
	public ClusterMulticastEventListener loggingMulticastEventListener() {
		return new LoggingClusterMulticastEventListener();
	}

}
