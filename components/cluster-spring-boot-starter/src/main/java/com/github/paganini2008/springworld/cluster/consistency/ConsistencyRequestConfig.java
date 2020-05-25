package com.github.paganini2008.springworld.cluster.consistency;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.paganini2008.devtools.multithreads.Clock;
import com.github.paganini2008.springworld.cluster.multicast.ClusterMessageListener;

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
	public ClusterMessageListener requestPreparationRequest() {
		return new ConsistencyRequestPreparationRequest();
	}

	@Bean
	public ClusterMessageListener requestPreparationResponse() {
		return new ConsistencyRequestPreparationResponse();
	}

	@Bean
	public ClusterMessageListener requestCommitmentRequest() {
		return new ConsistencyRequestCommitmentRequest();
	}

	@Bean
	public ClusterMessageListener requestCommitmentResponse() {
		return new ConsistencyRequestCommitmentResponse();
	}

	@Bean
	public ClusterMessageListener requestLearningRequest() {
		return new ConsistencyRequestLearningRequest();
	}

	@Bean
	public ClusterMessageListener requestLearningResponse() {
		return new ConsistencyRequestLearningResponse();
	}
	
	@Bean
	public LeaderElection leaderElection() {
		return new LeaderElection();
	}

}
