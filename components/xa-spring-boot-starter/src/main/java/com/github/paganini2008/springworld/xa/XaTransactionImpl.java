package com.github.paganini2008.springworld.xa;

import java.util.UUID;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * XaTransactionImpl
 *
 * @author Fred Feng
 * @version 1.0
 */
@Slf4j
public class XaTransactionImpl implements XaTransaction {

	private final String xaId;
	private final String id;
	private final boolean starter;
	private final PlatformTransactionManager transactionManager;
	private final TransactionStatus transactionStatus;
	private final long startTime;

	XaTransactionImpl(String xaId, boolean starter, PlatformTransactionManager transactionManager, TransactionStatus transactionStatus) {
		this.xaId = xaId;
		this.id = UUID.randomUUID().toString().replace("-", "");
		this.starter = starter;
		this.transactionManager = transactionManager;
		this.transactionStatus = transactionStatus;
		this.startTime = System.currentTimeMillis();
	}

	public XaTransactionResponse commit() {
		DefaultXaTransactionResponse response;
		try {
			transactionManager.commit(transactionStatus);
			response = DefaultXaTransactionResponse.commit(xaId, id);
			response.setCompleted(true);
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			response = DefaultXaTransactionResponse.commit(xaId, id);
			response.setReason(e);
			response.setCompleted(false);
		}
		response.setElapsedTime(getElapsedTime());
		return response;
	}

	public XaTransactionResponse rollback() {
		DefaultXaTransactionResponse response;
		try {
			transactionManager.rollback(transactionStatus);
			response = DefaultXaTransactionResponse.rollback(xaId, id);
			response.setCompleted(true);
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			response = DefaultXaTransactionResponse.rollback(xaId, id);
			response.setReason(e);
			response.setCompleted(false);
		}
		response.setElapsedTime(getElapsedTime());
		return response;
	}

	public boolean isCompleted() {
		return transactionStatus.isCompleted();
	}

	public String getXaId() {
		return xaId;
	}

	public String getId() {
		return id;
	}

	public boolean isStarter() {
		return starter;
	}

	public long getElapsedTime() {
		return System.currentTimeMillis() - startTime;
	}

}
