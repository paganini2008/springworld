package com.github.paganini2008.springworld.cluster;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.github.paganini2008.springworld.cluster.election.FastLeaderElection;
import com.github.paganini2008.springworld.cluster.election.FastLeaderElectionListener;
import com.github.paganini2008.springworld.cluster.election.LeaderElection;
import com.github.paganini2008.springworld.cluster.election.LeaderElectionListener;

/**
 * 
 * ApplicationClusterConfig
 *
 * @author Fred Feng
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
	@ConditionalOnMissingBean(LeaderRecoveryCallback.class)
	public LeaderRecoveryCallback leaderRecoveryCallback() {
		return new DefaultLeaderRecoveryCallback();
	}
}
