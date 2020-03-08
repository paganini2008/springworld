package com.github.paganini2008.springworld.tx;

import com.github.paganini2008.devtools.db4j.Transaction;

/**
 * 
 * JdbcXaTransaction
 *
 * @author Fred Feng
 * @version 1.0
 */
public class JdbcXaTransaction extends JdbcTransaction implements XaTransaction {

	public JdbcXaTransaction(String xaId, String id, Transaction transaction, TransactionEventPublisher transactionEventPublisher) {
		super(id, transaction, transactionEventPublisher);
		this.xaId = xaId;
	}

	private final String xaId;

	@Override
	protected String getEventPublishedId() {
		return xaId;
	}

	@Override
	public String getXaId() {
		return xaId;
	}

}
