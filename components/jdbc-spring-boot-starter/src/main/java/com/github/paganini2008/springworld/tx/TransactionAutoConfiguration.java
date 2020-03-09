package com.github.paganini2008.springworld.tx;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.github.paganini2008.springworld.tx.openfeign.OpenFeignConfig;

/**
 * 
 * TransactionAutoConfiguration
 *
 * @author Fred Feng
 * @version 1.0
 */
@Configuration
@Import({ JdbcTransactionConfig.class, XaTransactionConfig.class, OpenFeignConfig.class })
public class TransactionAutoConfiguration {
	
	@Bean
	@ConditionalOnMissingBean(IdGenerator.class)
	public IdGenerator uuidIdGenerator() {
		return new UuidIdGenerator();
	}

	@Bean
	public TransactionEventPublisher transactionEventPublisher() {
		return new TransactionEventPublisher();
	}

	@Bean
	public TransactionEventListenerContainer transactionEventListenerContainer() {
		return new TransactionEventListenerContainer();
	}
}
