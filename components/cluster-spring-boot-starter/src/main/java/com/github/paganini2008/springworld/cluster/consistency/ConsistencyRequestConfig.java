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
@ConditionalOnProperty(value = "spring.application.cluster.multicast.enabled", havingValue = "true")
public class ConsistencyRequestConfig {

	@ConditionalOnMissingBean(Clock.class)
	@Bean(destroyMethod = "stop")
	public Clock clock() {
		return new Clock();
	}
	
	@Bean
	public Court court() {
		return new Court();
	}

	@Bean
	public ConsistencyRequestContext consistencyRequestContext() {
		return new ConsistencyRequestContext();
	}

	@Bean
	public ConsistencyRequestRound consistencyRequestRound() {
		return new ConsistencyRequestRound();
	}

	@Bean
	public ConsistencyRequestSerial consistencyRequestSerial() {
		return new ConsistencyRequestSerial();
	}

	@Bean
	public ConsistencyRequestSerialCache consistencyRequestSerialCache() {
		return new ConsistencyRequestSerialCache();
	}

	@Bean
	public ClusterMessageListener consistencyRequestPreparationRequest() {
		return new ConsistencyRequestPreparationRequest();
	}

	@Bean
	public ClusterMessageListener consistencyRequestPreparationResponse() {
		return new ConsistencyRequestPreparationResponse();
	}

	@Bean
	public ClusterMessageListener consistencyRequestCommitmentRequest() {
		return new ConsistencyRequestCommitmentRequest();
	}

	@Bean
	public ClusterMessageListener consistencyRequestCommitmentResponse() {
		return new ConsistencyRequestCommitmentResponse();
	}

	@Bean
	public ClusterMessageListener consistencyRequestLearningRequest() {
		return new ConsistencyRequestLearningRequest();
	}

	@Bean
	public ClusterMessageListener consistencyRequestLearningResponse() {
		return new ConsistencyRequestLearningResponse();
	}

}
