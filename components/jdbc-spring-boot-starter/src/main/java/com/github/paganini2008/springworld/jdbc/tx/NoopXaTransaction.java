package com.github.paganini2008.springworld.jdbc.tx;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 
 * NoopXaTransaction
 *
 * @author Fred Feng
 * @version 1.0
 */
public class NoopXaTransaction implements XaTransaction {

	private final String xaId;
	private final String id;
	private final long startTime;
	private final TransactionEventPublisher transactionEventPublisher;
	private final AtomicBoolean completed = new AtomicBoolean(false);

	public NoopXaTransaction(String xaId, String id, TransactionEventPublisher transactionEventPublisher) {
		this.xaId = xaId;
		this.id = id;
		this.startTime = System.currentTimeMillis();
		this.transactionEventPublisher = transactionEventPublisher;
	}

	@Override
	public boolean commit() {
		transactionEventPublisher.beforeCommit(xaId);
		completed.set(true);
		transactionEventPublisher.afterCommit(xaId, null);
		return true;
	}

	@Override
	public boolean rollback() {
		transactionEventPublisher.beforeRollback(xaId);
		completed.set(true);
		transactionEventPublisher.afterRollback(xaId, null);
		return true;
	}

	@Override
	public boolean isCompleted() {
		return completed.get();
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getXaId() {
		return xaId;
	}

	@Override
	public long getStartTime() {
		return startTime;
	}

}
