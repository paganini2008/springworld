package com.github.paganini2008.springworld.cluster.consistency;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.paganini2008.devtools.multithreads.Clock;
import com.github.paganini2008.springworld.cluster.multicast.MulticastMessageListener;
import com.github.paganini2008.springworld.cluster.multicast.ClusterMulticastConfig;

/**
 * 
 * ConsistencyRequestConfig
 *
 * @author Fred Feng
 * @since 1.0
 */
@Configuration
@ConditionalOnBean(ClusterMulticastConfig.class)
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
	public MulticastMessageListener consistencyRequestPreparationRequest() {
		return new ConsistencyRequestPreparationRequest();
	}

	@Bean
	public MulticastMessageListener consistencyRequestPreparationResponse() {
		return new ConsistencyRequestPreparationResponse();
	}

	@Bean
	public MulticastMessageListener consistencyRequestCommitmentRequest() {
		return new ConsistencyRequestCommitmentRequest();
	}

	@Bean
	public MulticastMessageListener consistencyRequestCommitmentResponse() {
		return new ConsistencyRequestCommitmentResponse();
	}

	@Bean
	public MulticastMessageListener consistencyRequestLearningRequest() {
		return new ConsistencyRequestLearningRequest();
	}

	@Bean
	public MulticastMessageListener consistencyRequestLearningResponse() {
		return new ConsistencyRequestLearningResponse();
	}

}
