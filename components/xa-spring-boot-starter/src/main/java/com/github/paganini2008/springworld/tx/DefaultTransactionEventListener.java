package com.github.paganini2008.springworld.tx;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * 
 * DefaultTransactionEventListener
 *
 * @author Fred Feng
 * @version 1.0
 */
public class DefaultTransactionEventListener implements TransactionEventListener, ApplicationContextAware {

	@Override
	public void beforeCommit(String id) {
		applicationContext.publishEvent(new TransactionEvent(this, id, TransactionPhase.BEFORE_COMMIT, null));
	}

	@Override
	public void afterCommit(String id, Throwable cause) {
		applicationContext.publishEvent(new TransactionEvent(this, id, TransactionPhase.AFTER_COMMIT, cause));
	}

	@Override
	public void beforeRollback(String id) {
		applicationContext.publishEvent(new TransactionEvent(this, id, TransactionPhase.BEFORE_ROLLBACK, null));
	}

	@Override
	public void afterRollback(String id, Throwable cause) {
		applicationContext.publishEvent(new TransactionEvent(this, id, TransactionPhase.AFTER_ROLLBACK, cause));
	}

	private ApplicationContext applicationContext;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

}
