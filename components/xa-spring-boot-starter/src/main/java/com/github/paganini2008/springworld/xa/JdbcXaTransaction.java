package com.github.paganini2008.springworld.xa;

import java.util.UUID;

import com.github.paganini2008.devtools.ExceptionUtils;
import com.github.paganini2008.devtools.beans.ToStringBuilder;
import com.github.paganini2008.devtools.db4j.JdbcOperations;
import com.github.paganini2008.devtools.db4j.Transaction;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * JdbcXaTransaction
 *
 * @author Fred Feng
 * @version 1.0
 */
@Slf4j
public class JdbcXaTransaction implements XaTransaction {

	private final String xaId;
	private final String id;
	private final long startTime;
	private final Transaction transaction;

	JdbcXaTransaction(String xaId, Transaction transaction) {
		this.xaId = xaId;
		this.id = UUID.randomUUID().toString().replace("-", "");
		this.startTime = System.currentTimeMillis();
		this.transaction = transaction;
	}

	public XaTransactionResponse commit() {
		DefaultXaTransactionResponse response;
		try {
			if (isCompleted()) {
				log.warn("Current transaction has been completed.");
			} else {
				transaction.commit();
			}
			response = DefaultXaTransactionResponse.commit(xaId, id);
			response.setCompleted(true);
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			response = DefaultXaTransactionResponse.commit(xaId, id);
			response.setReason(ExceptionUtils.toArray(e));
			response.setCompleted(false);
		} finally {
			transaction.close();
		}
		response.setElapsedTime(System.currentTimeMillis() - startTime);
		return response;
	}

	public XaTransactionResponse rollback() {
		DefaultXaTransactionResponse response;
		try {
			if (isCompleted()) {
				log.warn("Current transaction has been completed.");
			} else {
				transaction.rollback();
			}
			response = DefaultXaTransactionResponse.rollback(xaId, id);
			response.setCompleted(true);
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			response = DefaultXaTransactionResponse.rollback(xaId, id);
			response.setReason(ExceptionUtils.toArray(e));
			response.setCompleted(false);
		} finally {
			transaction.close();
		}
		response.setElapsedTime(System.currentTimeMillis() - startTime);
		return response;
	}

	public boolean isCompleted() {
		return transaction.isCompleted();
	}

	public String getXaId() {
		return xaId;
	}

	public String getId() {
		return id;
	}

	public long getStartTime() {
		return startTime;
	}

	public JdbcOperations getJdbcOperations() {
		return transaction;
	}

	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

}
