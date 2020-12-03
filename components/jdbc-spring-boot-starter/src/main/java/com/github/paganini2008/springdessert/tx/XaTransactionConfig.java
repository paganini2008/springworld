package com.github.paganini2008.springdessert.tx;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.paganini2008.devtools.db4j.SqlPlus;

/**
 * 
 * XaTransactionConfig
 *
 * @author Jimmy Hoff
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
	
	@Bean
	@ConditionalOnMissingBean(SqlPlus.class)
	public XaTransactionFactory noopXaTransactionFactory() {
		return new NoopXaTransactionFactory();
	}

	@Bean("xa-transaction-manager")
	public XaTransactionManager xaTransactionManager() {
		return new XaTransactionManager();
	}

	@Bean
	public XaTransactionalJoinPointProcessor xaTransactionalJoinPointProcessor() {
		return new XaTransactionalJoinPointProcessor();
	}

	@Bean
	public XaTransactionCompletionHandler xaTransactionCompletionHandler() {
		return new XaTransactionCompletionHandler();
	}

}
