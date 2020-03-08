package com.github.paganini2008.springworld.jdbc.tx;

/**
 * 
 * TransactionManager
 *
 * @author Fred Feng
 * @version 1.0
 */
public interface TransactionManager {

	Transaction currentTransaction();
	
	default String currentTransactionId() {
		return currentTransaction().getId();
	}

	void closeTransaction(String id);

	void registerEventListener(TransactionPhase transactionPhase, String id, TransactionEventListener eventListener);

}
