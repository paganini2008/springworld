package com.github.paganini2008.springworld.jdbc;

import javax.sql.DataSource;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.paganini2008.devtools.db4j.SqlPlus;

/**
 * 
 * Db4jConfig
 *
 * @author Fred Feng
 * @version 1.0
 */
@Configuration
@ConditionalOnClass(DaoProxyBeanFactory.class)
public class Db4jConfig {

	@Bean
	public SqlPlus sqlPlus(DataSource dataSource) {
		return new SqlPlus(dataSource);
	}

}
