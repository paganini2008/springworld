package com.github.paganini2008.springworld.tx;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.paganini2008.devtools.db4j.SqlPlus;

/**
 * 
 * XaTransactionConfig
 *
 * @author Fred Feng
 * @version 1.0
 */
@Configuration
@ConditionalOnWebApplication
public class XaTransactionConfig {

	@Bean
	@ConditionalOnBean(SqlPlus.class)
	public XaTransactionFactory jdbcXaTransactionFactory() {
		return new JdbcXaTransactionFactory();
	}

	@Bean("xa-transaction-manager")
	public XaTransactionManager xaTransactionManager() {
		return new XaTransactionManager();
	}

	@Bean
	public XaTransactionalJoinPointProcessor xaTransactionalJoinPointProcessor() {
		return new XaTransactionalJoinPointProcessor();
	}

	@ConditionalOnBean(FeignAutoConfiguration.class)
	@Bean
	public XaTransactionCompletionHandler xaTransactionCompletionHandler() {
		return new XaTransactionCompletionHandler();
	}

}
