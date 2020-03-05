package com.github.paganini2008.springworld.xa;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import com.github.paganini2008.devtools.db4j.JdbcOperations;

/**
 * 
 * NullableXaTransactionalProcessor
 *
 * @author Fred Feng
 * @version 1.0
 */
public class WrappedXaTransaction implements XaTransaction {

	private final String id;
	private final String xaId;
	private final long startTime;
	private final AtomicBoolean completed = new AtomicBoolean();

	public WrappedXaTransaction(String xaId) {
		this.id = UUID.randomUUID().toString().replace("-", "");
		this.xaId = xaId;
		this.startTime = System.currentTimeMillis();
	}

	@Override
	public XaTransactionResponse commit() {
		DefaultXaTransactionResponse response = DefaultXaTransactionResponse.commit(xaId, id);
		response.setCompleted(true);
		completed.set(true);
		return response;
	}

	@Override
	public XaTransactionResponse rollback() {
		DefaultXaTransactionResponse response = DefaultXaTransactionResponse.rollback(xaId, id);
		response.setCompleted(true);
		completed.set(true);
		return response;
	}

	@Override
	public boolean isCompleted() {
		return completed.get();
	}

	@Override
	public String getXaId() {
		return xaId;
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
		throw new UnsupportedOperationException();
	}

}
