package com.github.paganini2008.springdessert.cluster.multicast;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import com.github.paganini2008.springdessert.cluster.ApplicationClusterController;
import com.github.paganini2008.springdessert.cluster.ApplicationClusterLoadBalancer;
import com.github.paganini2008.springdessert.cluster.Constants;
import com.github.paganini2008.springdessert.cluster.DefaultInstanceIdGenerator;
import com.github.paganini2008.springdessert.cluster.InstanceId;
import com.github.paganini2008.springdessert.cluster.InstanceIdGenerator;
import com.github.paganini2008.springdessert.cluster.LeaderContext;
import com.github.paganini2008.springdessert.cluster.RedisConnectionFailureHandler;
import com.github.paganini2008.springdessert.cluster.utils.LoadBalancer;

/**
 * 
 * ApplicationMulticastConfig
 * 
 * @author Jimmy Hoff
 *
 * @since 1.0
 */
@Configuration
@ConditionalOnProperty(value = "spring.application.cluster.multicast.enabled", havingValue = "true", matchIfMissing = true)
@Import({ ApplicationMulticastController.class, ApplicationClusterController.class })
public class ApplicationMulticastConfig {

	@Value("${spring.application.cluster.name}")
	private String clusterName;

	@Bean
	@ConditionalOnMissingBean
	public InstanceIdGenerator instanceIdGenerator() {
		return new DefaultInstanceIdGenerator();
	}

	@Bean
	public InstanceId instanceId() {
		return new InstanceId();
	}

	@Bean
	public LeaderContext leaderContext() {
		return new LeaderContext();
	}

	@Bean
	public RedisConnectionFailureHandler redisConnectionFailureHandler() {
		return new RedisConnectionFailureHandler();
	}

	@ConditionalOnMissingBean
	@Bean(destroyMethod = "shutdown")
	public TaskScheduler taskScheduler() {
		ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
		threadPoolTaskScheduler.setPoolSize(8);
		threadPoolTaskScheduler.setThreadNamePrefix("spring-application-cluster-task-scheduler-");
		threadPoolTaskScheduler.setWaitForTasksToCompleteOnShutdown(true);
		threadPoolTaskScheduler.setAwaitTerminationSeconds(60);
		return threadPoolTaskScheduler;
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
	public ApplicationClusterListenerContainer applicationClusterListenerContainer() {
		return new ApplicationClusterListenerContainer();
	}

	@Bean
	public ApplicationMulticastListener loggingApplicationClusterListener() {
		return new LoggingApplicationClusterListener();
	}

}
