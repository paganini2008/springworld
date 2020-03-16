package com.github.paganini2008.springworld.jdbc;

import javax.sql.DataSource;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.paganini2008.devtools.db4j.SqlPlus;
import com.github.paganini2008.springworld.tx.SessionManager;

/**
 * 
 * DaoConfig
 *
 * @author Fred Feng
 * @version 1.0
 */
@Configuration
@ConditionalOnBean(DataSource.class)
public class DaoConfig {

	@Bean
	public SqlPlus sqlPlus(DataSource dataSource) {
		return new SqlPlus(dataSource);
	}
	
	@Bean
	public SessionManager sessionManager() {
		return new SessionManager();
	}
	
}
