package com.github.paganini2008.springworld.tx.jdbc;

/**
 * 
 * XaTransactionImpl
 *
 * @author Fred Feng
 * @version 1.0
 */
public class XaTransactionImpl implements XaTransaction {

	public XaTransactionImpl(String xaId, Transaction transaction) {
		this.xaId = xaId;
		this.transaction = transaction;
	}

	private final String xaId;
	private final Transaction transaction;

	@Override
	public boolean commit() {
		return transaction.commit();
	}

	@Override
	public boolean rollback() {
		return transaction.rollback();
	}

	@Override
	public boolean isCompleted() {
		return transaction.isCompleted();
	}

	@Override
	public String getId() {
		return transaction.getId();
	}

	@Override
	public long getStartTime() {
		return transaction.getStartTime();
	}

	@Override
	public String getXaId() {
		return xaId;
	}

	@Override
	public Transaction getTransaction() {
		return transaction;
	}

}
