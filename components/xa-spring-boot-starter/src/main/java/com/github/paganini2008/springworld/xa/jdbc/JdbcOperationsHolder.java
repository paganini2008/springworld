package com.github.paganini2008.springworld.xa.jdbc;

import org.springframework.beans.factory.annotation.Autowired;

import com.github.paganini2008.devtools.db4j.JdbcOperations;
import com.github.paganini2008.devtools.db4j.SqlPlus;

/**
 * 
 * JdbcOperationsHolder
 *
 * @author Fred Feng
 * @version 1.0
 */
public class JdbcOperationsHolder extends ThreadLocal<JdbcOperations> {

	@Autowired
	private SqlPlus sqlPlus;

	@Override
	protected JdbcOperations initialValue() {
		return sqlPlus;
	}
	
	public void reset() {
		remove();
	}

}
