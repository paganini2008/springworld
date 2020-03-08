package com.github.paganini2008.springworld.jdbc.tx;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * TransactionEventPublisher
 *
 * @author Fred Feng
 * @version 1.0
 */
@Slf4j
public class TransactionEventPublisher implements ApplicationContextAware {

	public void afterCreate(String id) {
		if (log.isTraceEnabled()) {
			log.trace("Transaction: {} is created.", id);
		}
		applicationContext.publishEvent(new TransactionEvent(this, id, TransactionPhase.AFTER_CREATE, null));
	}

	public void beforeClose(String id) {
		if (log.isTraceEnabled()) {
			log.trace("Transaction: {} will close.", id);
		}
		applicationContext.publishEvent(new TransactionEvent(this, id, TransactionPhase.BEFORE_CLOSE, null));
	}

	public void beforeCommit(String id) {
		if (log.isTraceEnabled()) {
			log.trace("Transaction: {} will commit.", id);
		}
		applicationContext.publishEvent(new TransactionEvent(this, id, TransactionPhase.BEFORE_COMMIT, null));
	}

	public void afterCommit(String id, Throwable cause) {
		if (log.isTraceEnabled()) {
			log.trace("Transaction: " + id + " is committed.", cause);
		}
		applicationContext.publishEvent(new TransactionEvent(this, id, TransactionPhase.AFTER_COMMIT, cause));
	}

	public void beforeRollback(String id) {
		if (log.isTraceEnabled()) {
			log.trace("Transaction: {} will rollback.", id);
		}
		applicationContext.publishEvent(new TransactionEvent(this, id, TransactionPhase.BEFORE_ROLLBACK, null));
	}

	public void afterRollback(String id, Throwable cause) {
		if (log.isTraceEnabled()) {
			log.trace("Transaction: " + id + " is rollback.", cause);
		}
		applicationContext.publishEvent(new TransactionEvent(this, id, TransactionPhase.AFTER_ROLLBACK, cause));
	}

	private ApplicationContext applicationContext;

	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

}
