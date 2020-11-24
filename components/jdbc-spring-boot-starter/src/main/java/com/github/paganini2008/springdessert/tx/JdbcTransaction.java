package com.github.paganini2008.springdessert.tx;

import com.github.paganini2008.devtools.db4j.JdbcOperations;
import com.github.paganini2008.devtools.db4j.Transaction;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * JdbcTransaction
 *
 * @author Fred Feng
 * @version 1.0
 */
@Slf4j
public class JdbcTransaction implements com.github.paganini2008.springdessert.tx.Transaction, JdbcOperationsAware {

	private final Transaction transaction;
	private final String id;
	private final long startTime;
	private final TransactionEventPublisher transactionEventPublisher;

	public JdbcTransaction(String id, Transaction transaction, TransactionEventPublisher transactionEventPublisher) {
		this.transaction = transaction;
		this.id = id;
		this.startTime = System.currentTimeMillis();
		this.transactionEventPublisher = transactionEventPublisher;
	}

	@Override
	public boolean commit() {
		boolean success = true;
		Throwable cause = null;
		try {
			if (isCompleted()) {
				log.warn("Current transaction has been completed.");
			} else {
				transactionEventPublisher.beforeCommit(getEventPublishedId());
				transaction.commit();
			}
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			success = false;
			cause = e;
		} finally {
			transactionEventPublisher.afterCommit(getEventPublishedId(), cause);
			transaction.close();
		}
		return success;
	}

	@Override
	public boolean rollback() {
		boolean success = true;
		Throwable cause = null;
		try {
			if (isCompleted()) {
				log.warn("Current transaction has been completed.");
			} else {
				transactionEventPublisher.beforeRollback(getEventPublishedId());
				transaction.rollback();
			}
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			success = false;
			cause = e;
		} finally {
			transactionEventPublisher.afterRollback(getEventPublishedId(), cause);
			transaction.close();
		}
		return success;
	}

	protected String getEventPublishedId() {
		return id;
	}

	@Override
	public boolean isCompleted() {
		return transaction.isCompleted();
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public long getStartTime() {
		return startTime;
	}

	@Override
	public JdbcOperations getJdbcOperations() {
		return transaction;
	}

}
