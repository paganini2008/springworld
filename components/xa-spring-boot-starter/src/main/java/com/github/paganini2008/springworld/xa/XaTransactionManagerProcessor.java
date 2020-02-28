package com.github.paganini2008.springworld.xa;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;

import com.github.paganini2008.springworld.redis.pubsub.RedisMessageSender;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * XaTransactionManagerProcessor
 *
 * @author Fred Feng
 * @version 1.0
 */
@Slf4j
@Aspect
public class XaTransactionManagerProcessor {

	@Autowired
	private XaTransactionManager transactionManager;

	@Autowired
	private RedisMessageSender redisMessageSender;

	@Pointcut("execution(public * *(..))")
	public void signature() {
	}

	@Around("signature() && @annotation(com.github.paganini2008.springworld.xa.XaTransactional)")
	public Object arround(ProceedingJoinPoint pjp) throws Throwable {
		boolean ok = true;
		XaTransaction transaction = transactionManager.openTransaction();
		try {
			return pjp.proceed();
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			ok = false;
			throw e;
		} finally {
			redisMessageSender.subscribeChannel(transaction.getId(),
					new XaTransactionConfirmation(transaction, transactionManager, redisMessageSender));

			XaTransactionContext context = null;
			if (transaction.isStarter()) {
				redisMessageSender.subscribeChannel(transaction.getXaId(), new XaTransactionCompletion());
				context = XaTransactionContext.current();
				context.setOk(ok);
				context.await(transaction);
				redisMessageSender.unsubscribeChannel(transaction.getXaId());
			}

			if (context != null) {
				context.destroy();
			}
			transactionManager.closeTransaction();

		}
	}

}
