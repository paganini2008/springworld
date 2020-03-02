package com.github.paganini2008.springworld.xa;

import java.util.UUID;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

import com.github.paganini2008.devtools.ExceptionUtils;
import com.github.paganini2008.devtools.beans.ToStringBuilder;

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
	private final PlatformTransactionManager transactionManager;
	private final TransactionStatus transactionStatus;
	private final long startTime;

	XaTransactionImpl(String xaId, PlatformTransactionManager transactionManager, TransactionStatus transactionStatus) {
		this.xaId = xaId;
		this.id = UUID.randomUUID().toString().replace("-", "");
		this.transactionManager = transactionManager;
		this.transactionStatus = transactionStatus;
		this.startTime = System.currentTimeMillis();
	}

	public XaTransactionResponse commit() {
		DefaultXaTransactionResponse response;
		try {
			if (isCompleted()) {
				log.warn("Current transaction has been completed.");
			} else {
				transactionManager.commit(transactionStatus);
			}
			response = DefaultXaTransactionResponse.commit(xaId, id);
			response.setCompleted(true);
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			response = DefaultXaTransactionResponse.commit(xaId, id);
			response.setReason(ExceptionUtils.toArray(e));
			response.setCompleted(false);
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
				transactionManager.rollback(transactionStatus);
			}
			response = DefaultXaTransactionResponse.rollback(xaId, id);
			response.setCompleted(true);
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			response = DefaultXaTransactionResponse.rollback(xaId, id);
			response.setReason(ExceptionUtils.toArray(e));
			response.setCompleted(false);
		}
		response.setElapsedTime(System.currentTimeMillis() - startTime);
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

	public long getStartTime() {
		return startTime;
	}

	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

}
