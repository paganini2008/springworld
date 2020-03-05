package com.github.paganini2008.springworld.xa;

/**
 * 
 * XaTransactionFactory 
 *
 * @author Fred Feng
 * @version 1.0
 */
public interface XaTransactionFactory {

	XaTransaction createTransaction(String xaId);
	
}
