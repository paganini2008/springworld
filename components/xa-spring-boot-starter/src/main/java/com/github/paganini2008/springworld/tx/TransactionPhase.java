package com.github.paganini2008.springworld.tx;

/**
 * 
 * TransactionPhase
 *
 * @author Fred Feng
 * @version 1.0
 */
public enum TransactionPhase {

	BEFORE_COMMIT,

	AFTER_COMMIT,
	
	BEFORE_ROLLBACK,

	AFTER_ROLLBACK;
	
}
