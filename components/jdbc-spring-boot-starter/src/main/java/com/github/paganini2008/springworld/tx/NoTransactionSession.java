package com.github.paganini2008.springworld.tx;

import com.github.paganini2008.devtools.cache.Cache;
import com.github.paganini2008.devtools.db4j.JdbcOperations;

/**
 * 
 * NoTransactionSession
 *
 * @author Fred Feng
 * @version 1.0
 */
public class NoTransactionSession extends AbstractSession {

	public NoTransactionSession(JdbcOperations jdbcOperations, Cache cache) {
		super(jdbcOperations, cache);
	}

}
