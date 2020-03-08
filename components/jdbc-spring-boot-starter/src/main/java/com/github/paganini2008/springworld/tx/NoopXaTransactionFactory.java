package com.github.paganini2008.springworld.tx;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * NoopXaTransactionFactory
 *
 * @author Fred Feng
 * @version 1.0
 */
public class NoopXaTransactionFactory implements XaTransactionFactory {

	@Autowired
	private TransactionEventPublisher transactionEventPublisher;

	@Autowired
	private IdGenerator idGenerator;

	@Override
	public XaTransaction newTransaction(String xaId) {
		return new NoopXaTransaction(xaId, idGenerator.generateTransactionId(), transactionEventPublisher);
	}

}
