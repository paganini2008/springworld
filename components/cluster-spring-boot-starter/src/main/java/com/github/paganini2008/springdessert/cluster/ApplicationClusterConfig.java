package com.github.paganini2008.springdessert.cluster;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import com.github.paganini2008.springdessert.cluster.election.FastLeaderElection;
import com.github.paganini2008.springdessert.cluster.election.FastLeaderElectionListener;
import com.github.paganini2008.springdessert.cluster.election.LeaderElection;
import com.github.paganini2008.springdessert.cluster.election.LeaderElectionListener;

/**
 * 
 * ApplicationClusterConfig
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
@Configuration
@ConditionalOnProperty(value = "spring.application.cluster.enabled", havingValue = "true", matchIfMissing = true)
@Import({ ApplicationClusterController.class })
public class ApplicationClusterConfig {

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
	public ApplicationClusterAware clusterAware() {
		return new ApplicationClusterAware();
	}

	@Bean
	public LeaderContext leaderContext() {
		return new LeaderContext();
	}

	@ConditionalOnMissingBean(LeaderElectionListener.class)
	@Bean
	public LeaderElectionListener fastLeaderElectionListener() {
		return new FastLeaderElectionListener();
	}

	@ConditionalOnMissingBean(LeaderElection.class)
	@Bean
	public LeaderElection leaderElection() {
		return new FastLeaderElection();
	}

	@Bean
	public LeaderRecoveryCallback leaderRecoveryCallback() {
		return new UnsafeLeaderRecoveryCallback();
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
}
