package com.github.paganini2008.springdessert.tx;

/**
 * 
 * TransactionPhase
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
public enum TransactionPhase {
	
	AFTER_CREATE,

	BEFORE_COMMIT,

	AFTER_COMMIT,
	
	BEFORE_ROLLBACK,

	AFTER_ROLLBACK,
	
	BEFORE_CLOSE;
	
}
