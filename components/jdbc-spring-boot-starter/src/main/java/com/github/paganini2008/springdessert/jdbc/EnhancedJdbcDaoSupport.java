package com.github.paganini2008.springdessert.jdbc;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import org.springframework.lang.Nullable;

/**
 * 
 * EnhancedJdbcDaoSupport
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
public class EnhancedJdbcDaoSupport extends NamedParameterJdbcDaoSupport {

	@Nullable
	private EnhancedJdbcTemplate jdbcTemplate;

	@Override
	protected void initTemplateConfig() {
		JdbcTemplate jdbcTemplate = getJdbcTemplate();
		if (jdbcTemplate != null) {
			this.jdbcTemplate = new EnhancedJdbcTemplate(jdbcTemplate);
		}
	}

	@Override
	public EnhancedJdbcTemplate getNamedParameterJdbcTemplate() {
		return this.jdbcTemplate;
	}

}
