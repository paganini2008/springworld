package com.github.paganini2008.springdessert.tx;

/**
 * 
 * XaTransactionFactory
 *
 * @author Fred Feng
 * @version 1.0
 */
public interface XaTransactionFactory {

	XaTransaction newTransaction(String xaId);
	
}
