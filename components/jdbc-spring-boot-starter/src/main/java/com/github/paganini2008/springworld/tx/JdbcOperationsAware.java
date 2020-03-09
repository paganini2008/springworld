package com.github.paganini2008.springworld.tx;

import com.github.paganini2008.devtools.db4j.JdbcOperations;

/**
 * 
 * JdbcOperationsAware
 *
 * @author Fred Feng
 * @version 1.0
 */
public interface JdbcOperationsAware {

	JdbcOperations getJdbcOperations();
	
}
