package com.github.paganini2008.springworld.jdbc.tx;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.paganini2008.devtools.db4j.SqlPlus;

/**
 * 
 * JdbcTransactionConfig
 *
 * @author Fred Feng
 * @version 1.0
 */
@Configuration
@ConditionalOnBean(SqlPlus.class)
public class JdbcTransactionConfig {

	@Bean
	public TransactionFactory jdbcTransactionFactory() {
		return new JdbcTransactionFactory();
	}

	@Bean
	public TransactionJoinPointProcessor transactionJoinPointProcessor() {
		return new TransactionJoinPointProcessor();
	}

	@Bean
	public SessionManager sessionManager() {
		return new SessionManager();
	}

	@Bean("jdbc-transaction-manager")
	public TransactionManager jdbcTransactionManager() {
		return new JdbcTransactionManager();
	}

}
