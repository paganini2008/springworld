package com.github.paganini2008.springworld.tx;

import org.springframework.beans.factory.annotation.Autowired;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * JdbcTransactionManager
 *
 * @author Fred Feng
 * @version 1.0
 */
@Slf4j
public class JdbcTransactionManager implements TransactionManager {

	private final ThreadLocal<Transaction> threadLocal = new ThreadLocal<Transaction>() {

		@Override
		protected Transaction initialValue() {
			final String id = idGenerator.generateTransactionId();
			Transaction newTransaction = transactionFactory.newTransaction(id);
			if (log.isTraceEnabled()) {
				log.trace("New transaction: " + newTransaction.toString());
			}
			transactionEventPublisher.afterCreate(id);
			return newTransaction;
		}

	};

	@Autowired
	private TransactionFactory transactionFactory;

	@Autowired
	private IdGenerator idGenerator;

	@Autowired
	private TransactionEventPublisher transactionEventPublisher;

	@Autowired
	private TransactionEventListenerContainer eventListenerContainer;

	@Override
	public Transaction currentTransaction() {
		Transaction transaction = threadLocal.get();
		if (log.isTraceEnabled()) {
			log.trace("Current transaction: " + transaction.toString());
		}
		return transaction;
	}

	@Override
	public void registerEventListener(TransactionPhase transactionPhase, String id, TransactionEventListener eventListener) {
		eventListenerContainer.registerEventListener(transactionPhase, id, eventListener);
	}

	@Override
	public void closeTransaction(String id) {
		transactionEventPublisher.beforeClose(id);
		
		Transaction transaction = threadLocal.get();
		if (transaction != null) {
			if (!transaction.isCompleted()) {
				transaction.rollback();
			}
			threadLocal.remove();
			if (log.isTraceEnabled()) {
				log.trace("Close transaction: " + transaction.toString());
			}
		}
	}

}
