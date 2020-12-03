package com.github.paganini2008.springdessert.tx;

import com.github.paganini2008.devtools.db4j.JdbcOperations;

/**
 * 
 * JdbcOperationsAware
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
public interface JdbcOperationsAware {

	JdbcOperations getJdbcOperations();
	
}
