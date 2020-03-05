package com.github.paganini2008.springworld.xa;

import com.github.paganini2008.devtools.db4j.JdbcOperations;

/**
 * 
 * XaTransaction
 *
 * @author Fred Feng
 * @version 1.0
 */
public interface XaTransaction {

	XaTransactionResponse commit();

	XaTransactionResponse rollback();

	boolean isCompleted();

	String getXaId();

	String getId();

	long getStartTime();
	
	JdbcOperations getJdbcOperations();
}