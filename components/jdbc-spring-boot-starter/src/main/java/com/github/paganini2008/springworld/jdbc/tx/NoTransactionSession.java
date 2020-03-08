package com.github.paganini2008.springworld.jdbc.tx;

import com.github.paganini2008.devtools.db4j.JdbcOperations;

/**
 * 
 * NoTransactionSession
 *
 * @author Fred Feng
 * @version 1.0
 */
public class NoTransactionSession implements Session {

	private final JdbcOperations jdbcOperations;

	public NoTransactionSession(JdbcOperations jdbcOperations) {
		this.jdbcOperations = jdbcOperations;
	}

	@Override
	public JdbcOperations getJdbcOperations() {
		return jdbcOperations;
	}

}
