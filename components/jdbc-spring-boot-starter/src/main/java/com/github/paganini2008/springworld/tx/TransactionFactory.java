package com.github.paganini2008.springworld.tx;

/**
 * 
 * TransactionFactory
 *
 * @author Fred Feng
 * @version 1.0
 */
public interface TransactionFactory {

	Transaction newTransaction(String id);

}
