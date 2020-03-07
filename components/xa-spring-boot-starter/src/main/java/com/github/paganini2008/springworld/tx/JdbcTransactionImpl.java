package com.github.paganini2008.springworld.tx;

import com.github.paganini2008.devtools.db4j.JdbcOperations;
import com.github.paganini2008.devtools.db4j.Transaction;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * JdbcTransactionImpl
 *
 * @author Fred Feng
 * @version 1.0
 */
@Slf4j
public class JdbcTransactionImpl implements JdbcTransaction {

	private final Transaction transaction;
	private final String id;
	private final long startTime;
	private final TransactionEventListener transactionEventListener;

	public JdbcTransactionImpl(String id, Transaction transaction, TransactionEventListener transactionEventListener) {
		this.transaction = transaction;
		this.id = id;
		this.startTime = System.currentTimeMillis();
		this.transactionEventListener = transactionEventListener;
	}

	@Override
	public boolean commit() {
		boolean success = true;
		Throwable cause = null;
		try {
			if (isCompleted()) {
				log.warn("Current transaction has been completed.");
			} else {
				transactionEventListener.beforeCommit(id);
				transaction.commit();
			}
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			success = false;
			cause = e;
		} finally {
			transactionEventListener.afterCommit(id, cause);
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
				transactionEventListener.beforeRollback(id);
				transaction.rollback();
			}
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			success = false;
			cause = e;
		} finally {
			transactionEventListener.afterRollback(id, cause);
			transaction.close();
		}
		return success;
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
