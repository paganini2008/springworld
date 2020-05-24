package com.github.paganini2008.springworld.cluster.consistency;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.paganini2008.devtools.multithreads.Clock;
import com.github.paganini2008.springworld.cluster.multicast.ClusterMulticastEventListener;

/**
 * 
 * ConsistencyRequestConfig
 *
 * @author Fred Feng
 * @since 1.0
 */
@Configuration
@ConditionalOnProperty(value = "spring.application.cluster.lead-election.enabled", havingValue = "true")
public class ConsistencyRequestConfig {

	@ConditionalOnMissingBean(Clock.class)
	@Bean
	public Clock clock() {
		return new Clock();
	}

	@Bean
	public ConsistencyRequestContext requestContext() {
		return new ConsistencyRequestContext();
	}

	@Bean
	public ConsistencyRequestRound requestRound() {
		return new ConsistencyRequestRound();
	}

	@Bean
	public ConsistencyRequestSerial requestSerial() {
		return new ConsistencyRequestSerial();
	}

	@Bean
	public ConsistencyRequestSerialCache requestSerialCache() {
		return new ConsistencyRequestSerialCache();
	}

	@Bean
	public ClusterMulticastEventListener requestPreparationRequest() {
		return new ConsistencyRequestPreparationRequest();
	}

	@Bean
	public ClusterMulticastEventListener requestPreparationResponse() {
		return new ConsistencyRequestPreparationResponse();
	}

	@Bean
	public ClusterMulticastEventListener requestCommitmentRequest() {
		return new ConsistencyRequestCommitmentRequest();
	}

	@Bean
	public ClusterMulticastEventListener requestCommitmentResponse() {
		return new ConsistencyRequestCommitmentResponse();
	}

	@Bean
	public ClusterMulticastEventListener requestLearningRequest() {
		return new ConsistencyRequestLearningRequest();
	}

	@Bean
	public ClusterMulticastEventListener requestLearningResponse() {
		return new ConsistencyRequestLearningResponse();
	}
	
	@Bean
	public LeaderElection leaderElection() {
		return new LeaderElection();
	}

}
