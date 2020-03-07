package com.github.paganini2008.springworld.tx;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.github.paganini2008.devtools.multithreads.ThreadLocalInteger;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * TransactionJoinPointProcessor
 *
 * @author Fred Feng
 * @version 1.0
 */
@Slf4j
@Aspect
public class TransactionJoinPointProcessor {

	@Qualifier("local-transaction-manager")
	@Autowired
	private TransactionManager transactionManager;

	@Autowired
	private SessionManager sessionManager;

	private final ThreadLocalInteger nestable = new ThreadLocalInteger(0);

	@Pointcut("execution(public * *(..))")
	public void signature() {
	}

	@SuppressWarnings("unchecked")
	@Around("signature() && @annotation(com.github.paganini2008.springworld.tx.Transactional)")
	public Object arround(ProceedingJoinPoint pjp) throws Throwable {
		final Transactional transactionDefinition = (Transactional) pjp.getSignature().getDeclaringType()
				.getAnnotation(Transactional.class);
		Transaction transaction = transactionManager.openTransaction();
		if (nestable.incrementAndGet() == 1) {
			sessionManager.set(new TransactionalSession((JdbcTransaction) transaction));
		}
		boolean ok = true;
		Throwable cause = null;
		try {
			return pjp.proceed();
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			ok = false;
			cause = e;
			throw e;
		} finally {
			if (!isNestable()) {
				if (ok || dontRollback(cause, transactionDefinition)) {
					transaction.commit();
				} else {
					transaction.rollback();
				}
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
