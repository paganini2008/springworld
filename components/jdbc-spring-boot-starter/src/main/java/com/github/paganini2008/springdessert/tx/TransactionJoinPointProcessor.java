package com.github.paganini2008.springdessert.tx;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.github.paganini2008.devtools.StringUtils;
import com.github.paganini2008.devtools.db4j.TransactionException;
import com.github.paganini2008.devtools.multithreads.ThreadLocalInteger;
import com.github.paganini2008.devtools.reflection.MethodUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * TransactionJoinPointProcessor
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
@Slf4j
@Aspect
public class TransactionJoinPointProcessor {

	@Qualifier("jdbc-transaction-manager")
	@Autowired
	private TransactionManager transactionManager;

	@Autowired
	private SessionManager sessionManager;

	@Autowired
	private TransactionEventListenerContainer listenerContainer;

	private final ThreadLocalInteger nestable = new ThreadLocalInteger(0);

	@Pointcut("execution(public * *(..))")
	public void signature() {
	}

	@Around("signature() && @annotation(com.github.paganini2008.springworld.tx.Transactional)")
	public Object arround(ProceedingJoinPoint pjp) throws Throwable {
		MethodSignature signature = (MethodSignature) pjp.getSignature();
		if (log.isTraceEnabled()) {
			log.trace(signature.toString());
		}
		Transactional transactionDefinition = signature.getMethod().getAnnotation(Transactional.class);
		Transaction transaction = transactionManager.currentTransaction();
		if (nestable.incrementAndGet() == 1) {
			sessionManager.bindTransaction((JdbcTransaction) transaction, transactionDefinition.timeout());
			if (transactionDefinition.subscribeEvent() != null) {
				if (StringUtils.isNotBlank(transactionDefinition.eventHandler())) {
					listenerContainer.registerEventListener(transactionDefinition.subscribeEvent(), transaction.getId(), event -> {
						try {
							MethodUtils.invokeMethod(pjp.getTarget(), transactionDefinition.eventHandler(), event);
						} catch (RuntimeException ignored) {
							log.warn(ignored.getMessage(), ignored);
						}
					});
				}
			}
		}
		boolean ok = true;
		Throwable cause = null;
		try {
			return pjp.proceed();
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			ok = false;
			cause = e;
			if (e instanceof TransactionException) {
				throw (TransactionException) e;
			}
			throw new TransactionException(e);
		} finally {
			if (!isNestable()) {
				boolean completed;
				if (ok || dontRollback(cause, transactionDefinition)) {
					completed = transaction.commit();
				} else {
					completed = transaction.rollback();
				}
				if (!completed) {
					if (log.isTraceEnabled()) {
						log.trace("The transaction: {} is not completed.", transaction);
					}
				}
				transactionManager.closeTransaction(transaction.getId());
				sessionManager.reset();
			}
		}
	}

	private boolean isNestable() {
		if (nestable.get() == 1) {
			nestable.reset();
			return false;
		}
		nestable.decrementAndGet();
		return true;
	}

	private boolean dontRollback(Throwable cause, Transactional transactionDefinition) {
		if (cause == null) {
			return false;
		}
		Class<?>[] exceptionClasses = transactionDefinition.rollbackFor();
		if (exceptionClasses != null) {
			for (Class<?> exceptionClass : exceptionClasses) {
				if (exceptionClass.isAssignableFrom(cause.getClass())) {
					return false;
				}
			}
		}
		return true;
	}

}
