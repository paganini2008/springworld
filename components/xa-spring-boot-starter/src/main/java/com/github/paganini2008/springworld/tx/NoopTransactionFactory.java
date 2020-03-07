package com.github.paganini2008.springworld.tx;

/**
 * 
 * NoopTransactionFactory
 *
 * @author Fred Feng
 * @version 1.0
 */
public class NoopTransactionFactory implements TransactionFactory {

	@Override
	public Transaction createTransaction(String id) {
		return new NoopTransaction(id);
	}

}
