package com.github.paganini2008.springworld.tx;

import com.github.paganini2008.devtools.db4j.JdbcOperations;

/**
 * 
 * TransactionalSession
 *
 * @author Fred Feng
 * @version 1.0
 */
public class TransactionalSession implements Session {

	private final JdbcTransaction jdbcTransaction;

	public TransactionalSession(JdbcTransaction jdbcTransaction) {
		this.jdbcTransaction = jdbcTransaction;
	}

	@Override
	public JdbcOperations getJdbcOperations() {
		return jdbcTransaction.getJdbcOperations();
	}

}
