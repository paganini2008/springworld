package com.github.paganini2008.springworld.tx.jdbc;

/**
 * 
 * XaTransaction
 *
 * @author Fred Feng
 * @version 1.0
 */
public interface XaTransaction extends Transaction {

	String getXaId();
	
	Transaction getTransaction();

}
