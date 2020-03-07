package com.github.paganini2008.springworld.tx;

import org.springframework.beans.factory.annotation.Autowired;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * LocalTransactionManager
 *
 * @author Fred Feng
 * @version 1.0
 */
@Slf4j
public class LocalTransactionManager implements TransactionManager {

	private final ThreadLocal<Transaction> threadLocal = new ThreadLocal<Transaction>() {

		@Override
		protected Transaction initialValue() {
			final String id = transactionId.generateId();
			return transactionFactory.createTransaction(id);
		}

	};

	@Autowired
	private TransactionFactory transactionFactory;

	@Autowired
	private TransactionId transactionId;

	@Override
	public Transaction openTransaction() {
		Transaction transaction = threadLocal.get();
		if (log.isTraceEnabled()) {
			log.trace("Current transaction: " + transaction.toString());
		}
		return transaction;
	}

	@Override
	public void closeTransaction(String id) {
		threadLocal.remove();
	}

}
