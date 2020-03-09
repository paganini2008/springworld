package com.github.paganini2008.springworld.jdbc.optional;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcOperations;

/**
 * 
 * SpringDaoConfig
 *
 * @author Fred Feng
 * @version 1.0
 */
@Configuration
@ConditionalOnClass(SpringDaoProxyBeanFactory.class)
public class SpringDaoConfig {

	@Bean
	public EnhancedJdbcTemplate enhancedJdbcTemplate(JdbcOperations jdbcOperations) {
		return new EnhancedJdbcTemplate(jdbcOperations);
	}

}
