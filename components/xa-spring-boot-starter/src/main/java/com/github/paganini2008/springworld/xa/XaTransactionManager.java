package com.github.paganini2008.springworld.xa;

/**
 * 
 * XaTransactionManager 
 *
 * @author Fred Feng
 * @version 1.0
 */
public interface XaTransactionManager {

	XaTransaction openTransaction();
	
	void closeTransaction();

}