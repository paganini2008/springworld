package com.github.paganini2008.springworld.tx;

import static com.github.paganini2008.springworld.tx.XaTransactionManager.XA_HTTP_REQUEST_IDENTITY;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.github.paganini2008.devtools.ExceptionUtils;
import com.github.paganini2008.devtools.collection.CollectionUtils;
import com.github.paganini2008.devtools.multithreads.ThreadLocalInteger;
import com.github.paganini2008.springworld.redis.pubsub.RedisMessageHandler;
import com.github.paganini2008.springworld.redis.pubsub.RedisMessageSender;
import com.github.paganini2008.springworld.tx.jdbc.JdbcTransaction;
import com.github.paganini2008.springworld.tx.jdbc.SessionManager;
import com.github.paganini2008.springworld.tx.jdbc.TransactionManager;
import com.github.paganini2008.springworld.tx.jdbc.TransactionalSession;
import com.github.paganini2008.springworld.tx.jdbc.XaTransaction;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * XaTransactionalJoinPointProcessor
 *
 * @author Fred Feng
 * @version 1.0
 */
@Slf4j
@Aspect
public class XaTransactionalJoinPointProcessor {

	@Qualifier("xa-transaction-manager")
	@Autowired
	private TransactionManager transactionManager;

	@Autowired
	private SessionManager sessionManager;

	@Autowired
	private RedisMessageSender redisMessageSender;

	@Autowired
	private StringRedisTemplate redisTemplate;

	private final ThreadLocalInteger nestable = new ThreadLocalInteger(0);

	@Pointcut("execution(public * *(..))")
	public void signature() {
	}

	@SuppressWarnings("unchecked")
	@Around("signature() && @annotation(com.github.paganini2008.springworld.tx.XaTransactional)")
	public Object arround(ProceedingJoinPoint pjp) throws Throwable {
		final XaTransactional transactionDefinition = (XaTransactional) pjp.getSignature().getDeclaringType()
				.getAnnotation(XaTransactional.class);
		nestable.incrementAndGet();
		boolean ok = true;
		Throwable cause = null;
		XaTransaction transaction = (XaTransaction) transactionManager.openTransaction();
		bindSession(transaction);
		try {
			return pjp.proceed();
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			ok = false;
			cause = e;
			throw e;
		} finally {
			if (hasXaHeader()) {
				if (!isNestable()) {

					lazyCommit(transaction, transactionDefinition, cause);
					sessionManager.reset();
				}
			} else {
				if (!isNestable()) {
					boolean completed;
					if (ok || dontRollback(cause, transactionDefinition)) {
						completed = transaction.commit();
					} else {
						completed = transaction.rollback();
					}
					if (completed) {
						if (redisTemplate.hasKey(transaction.getXaId())) {
							List<String> transactionIds = redisTemplate.opsForList().range(transaction.getXaId(), 0, -1);
							if (CollectionUtils.isNotEmpty(transactionIds)) {
								for (String transactionId : transactionIds) {
									redisMessageSender.sendMessage("commitment:" + transaction.getXaId() + ":" + transactionId, ok);
								}
							}
							redisTemplate.delete(transaction.getXaId());
						}
					}

					sessionManager.reset();
					transactionManager.closeTransaction(transaction.getXaId());
				}
			}
		}
	}

	private void lazyCommit(final XaTransaction transaction, XaTransactional transactionDefinition, Throwable cause) {
		redisTemplate.opsForList().rightPush(transaction.getXaId(), transaction.getId());
		redisMessageSender.subscribeChannel(transaction.getId(), new RedisMessageHandler() {

			@Override
			public void onMessage(String channel, Object message) {
				boolean ok = (Boolean) message;
				boolean completed;
				if (ok || dontRollback(cause, transactionDefinition)) {
					completed = transaction.commit();
				} else {
					completed = transaction.rollback();
				}
				
				XaTransactionResponse response = new XaTransactionResponse();
				response.setId(transaction.getId());
				response.setXaId(transaction.getXaId());
				response.setCompleted(completed);
				response.setElapsedTime(System.currentTimeMillis() - transaction.getStartTime());
				response.setOk(ok);
				response.setReason(ExceptionUtils.toArray(cause));

				redisMessageSender.sendMessage("completion:" + transaction.getXaId() + ":" + transaction.getId(), response);
				redisMessageSender.unsubscribeChannel(transaction.getId());

				transactionManager.closeTransaction(transaction.getXaId());
			}

			@Override
			public String getChannel() {
				return "commitment:" + transaction.getXaId() + ":" + transaction.getId();
			}
		});
	}

	private void bindSession(XaTransaction transaction) {
		if (transaction.getTransaction() instanceof JdbcTransaction) {
			sessionManager.set(new TransactionalSession((JdbcTransaction) transaction.getTransaction()));
		}
	}

	private boolean hasXaHeader() {
		HttpServletRequest httpServletRequest = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
		return httpServletRequest.getParameter(XA_HTTP_REQUEST_IDENTITY) != null
				|| httpServletRequest.getHeader(XA_HTTP_REQUEST_IDENTITY) != null;
	}

	private boolean isNestable() {
		if (nestable.get() == 1) {
			nestable.reset();
			return false;
		}
		nestable.decrementAndGet();
		return true;
	}

	private boolean dontRollback(Throwable cause, XaTransactional transactionDefinition) {
		if (cause == null) {
			return true;
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
