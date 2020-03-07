package com.github.paganini2008.springworld.tx;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.github.paganini2008.devtools.db4j.SqlPlus;
import com.github.paganini2008.springworld.tx.jdbc.JdbcTransactionFactory;
import com.github.paganini2008.springworld.tx.jdbc.NoopTransactionFactory;
import com.github.paganini2008.springworld.tx.jdbc.TransactionFactory;
import com.github.paganini2008.springworld.tx.openfeign.OpenFeignConfig;

/**
 * 
 * XaTransactionAutoConfiguration
 *
 * @author Fred Feng
 * @version 1.0
 */
@Configuration
@ConditionalOnWebApplication
@Import({ ApplicationContextUtils.class, OpenFeignConfig.class })
public class XaTransactionAutoConfiguration {

	@Bean
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

	@Bean
	@ConditionalOnMissingBean(SqlPlus.class)
	public TransactionFactory noopTransactionFactory() {
		return new NoopTransactionFactory();
	}

	@Bean
	@ConditionalOnBean(SqlPlus.class)
	public TransactionFactory jdbcTransactionFactory() {
		return new JdbcTransactionFactory();
	}

}
