package com.github.paganini2008.springworld.jdbc.tx;

import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.github.paganini2008.devtools.db4j.SqlPlus;
import com.github.paganini2008.springworld.jdbc.tx.openfeign.OpenFeignConfig;

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

	@Value("${spring.application.transaction.executor.threadCount:8}")
	private int threadCount;

	@Bean
	@ConditionalOnMissingBean(IdGenerator.class)
	public IdGenerator uuidIdGenerator() {
		return new UuidIdGenerator();
	}

	@Bean
	@ConditionalOnMissingBean(SqlPlus.class)
	public XaTransactionFactory noopXaTransactionFactory() {
		return new NoopXaTransactionFactory();
	}

	@Bean
	public TransactionEventPublisher transactionEventPublisher() {
		return new TransactionEventPublisher();
	}

	@Bean
	public TransactionEventListenerContainer transactionEventListenerContainer() {
		return new TransactionEventListenerContainer();
	}

	@Bean("threadPoolBean")
	public ThreadPoolTaskExecutor threadPoolTaskExecutor() {
		ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
		taskExecutor.setCorePoolSize(threadCount);
		taskExecutor.setMaxPoolSize(threadCount);
		taskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
		return taskExecutor;
	}

}
