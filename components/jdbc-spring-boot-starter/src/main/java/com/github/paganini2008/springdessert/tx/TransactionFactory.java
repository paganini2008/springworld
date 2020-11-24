package com.github.paganini2008.springdessert.tx;

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
