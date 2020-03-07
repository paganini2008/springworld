package com.github.paganini2008.springworld.tx;

/**
 * 
 * TransactionEventListener
 *
 * @author Fred Feng
 * @version 1.0
 */
public interface TransactionEventListener {

	void beforeCommit(String id);

	void afterCommit(String id, Throwable cause);

	void beforeRollback(String id);

	void afterRollback(String id, Throwable cause);

}
