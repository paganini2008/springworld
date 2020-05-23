package com.github.paganini2008.springworld.cluster.consistency;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.paganini2008.devtools.multithreads.Clock;
import com.github.paganini2008.springworld.cluster.multicast.ContextMulticastEventHandler;

/**
 * 
 * ConsistencyRequestConfig
 *
 * @author Fred Feng
 * @since 1.0
 */
@Configuration
public class ConsistencyRequestConfig {

	@ConditionalOnBean(Clock.class)
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
	public ContextMulticastEventHandler requestPreparationRequest() {
		return new ConsistencyRequestPreparationRequest();
	}

	@Bean
	public ContextMulticastEventHandler requestPreparationResponse() {
		return new ConsistencyRequestPreparationResponse();
	}

	@Bean
	public ContextMulticastEventHandler requestCommitmentRequest() {
		return new ConsistencyRequestCommitmentRequest();
	}

	@Bean
	public ContextMulticastEventHandler requestCommitmentResponse() {
		return new ConsistencyRequestCommitmentResponse();
	}

	@Bean
	public ContextMulticastEventHandler requestLearningRequest() {
		return new ConsistencyRequestLearningRequest();
	}

	@Bean
	public ContextMulticastEventHandler requestLearningResponse() {
		return new ConsistencyRequestLearningResponse();
	}

}
