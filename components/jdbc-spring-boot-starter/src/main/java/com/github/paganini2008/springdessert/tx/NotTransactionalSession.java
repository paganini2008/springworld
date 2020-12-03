package com.github.paganini2008.springdessert.tx;

import com.github.paganini2008.devtools.cache.Cache;
import com.github.paganini2008.devtools.db4j.JdbcOperations;

/**
 * 
 * NotTransactionalSession
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
public class NotTransactionalSession extends AbstractSession {

	public NotTransactionalSession(JdbcOperations jdbcOperations, Cache cache) {
		super(jdbcOperations, cache);
	}

}
