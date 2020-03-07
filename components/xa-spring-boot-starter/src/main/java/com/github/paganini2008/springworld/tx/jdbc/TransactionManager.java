package com.github.paganini2008.springworld.tx.jdbc;

/**
 * 
 * TransactionManager
 *
 * @author Fred Feng
 * @version 1.0
 */
public interface TransactionManager {

	Transaction openTransaction();
	
	void closeTransaction(String id);
	
}
