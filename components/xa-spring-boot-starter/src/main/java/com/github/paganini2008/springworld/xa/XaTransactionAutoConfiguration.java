package com.github.paganini2008.springworld.xa;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.github.paganini2008.devtools.db4j.SqlPlus;
import com.github.paganini2008.springworld.xa.openfeign.OpenFeignConfig;

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
		return new DefaultXaTransactionManager();
	}

	@Bean
	public XaTransactionalProcessor xaTransactionalProcessor() {
		return new XaTransactionalProcessor();
	}

	@Bean
	public XaTransactionCompletionHandler xaTransactionCompletionHandler() {
		return new XaTransactionCompletionHandler();
	}

	@Bean
	@ConditionalOnMissingBean(SqlPlus.class)
	public XaTransactionFactory wrappedXaTransactionFactory() {
		return new WrappedXaTransactionFactory();
	}

	@Bean
	@ConditionalOnBean(SqlPlus.class)
	public XaTransactionFactory jdbcXaTransactionFactory() {
		return new JdbcXaTransactionFactory();
	}

}
