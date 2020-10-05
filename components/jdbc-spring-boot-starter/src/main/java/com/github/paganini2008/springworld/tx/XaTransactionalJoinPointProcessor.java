package com.github.paganini2008.springworld.tx;

import static com.github.paganini2008.springworld.tx.XaTransactionManager.XA_HTTP_REQUEST_IDENTITY;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.github.paganini2008.devtools.ExceptionUtils;
import com.github.paganini2008.devtools.StringUtils;
import com.github.paganini2008.devtools.collection.CollectionUtils;
import com.github.paganini2008.devtools.multithreads.ThreadLocalInteger;
import com.github.paganini2008.devtools.reflection.MethodUtils;
import com.github.paganini2008.springworld.reditools.messager.RedisMessageHandler;
import com.github.paganini2008.springworld.reditools.messager.RedisMessageSender;

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

	@Autowired(required = false)
	private SessionManager sessionManager;

	@Autowired
	private RedisMessageSender redisMessageSender;

	@Autowired
	private StringRedisTemplate redisTemplate;

	@Autowired
	private TransactionEventListenerContainer listenerContainer;

	private final ThreadLocalInteger nestable = new ThreadLocalInteger(0);

	@Pointcut("execution(public * *(..))")
	public void signature() {
	}

	@Around("signature() && @annotation(com.github.paganini2008.springworld.tx.XaTransactional)")
	public Object arround(ProceedingJoinPoint pjp) throws Throwable {
		MethodSignature signature = (MethodSignature) pjp.getSignature();
		XaTransactional transactionDefinition = signature.getMethod().getAnnotation(XaTransactional.class);
		if (log.isTraceEnabled()) {
			log.trace(signature.toString());
		}
		XaTransaction transaction = (XaTransaction) transactionManager.currentTransaction();
		if (nestable.incrementAndGet() == 1) {
			if (transaction instanceof JdbcTransaction && sessionManager != null) {
				sessionManager.bindTransaction((JdbcTransaction) transaction, transactionDefinition.timeout());
			}
			if (transactionDefinition.subscribeEvent() != null) {
				if (StringUtils.isNotBlank(transactionDefinition.eventHandler())) {
					listenerContainer.registerEventListener(transactionDefinition.subscribeEvent(), transaction.getXaId(), event -> {
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
			if (e instanceof XaTransactionException) {
				throw (XaTransactionException) e;
			}
			throw new XaTransactionException(e);
		} finally {
			if (!isNestable()) {
				if (hasXaHeader()) {
					lazyCommit(transaction, transactionDefinition, pjp.getSignature(), cause);
					if (sessionManager != null) {
						sessionManager.reset();
					}
				} else {
					boolean completed;
					if (ok || dontRollback(cause, transactionDefinition)) {
						completed = transaction.commit();
					} else {
						completed = transaction.rollback();
					}
					if (log.isTraceEnabled()) {
						log.trace("{}, ok? {}, completed? {}", pjp.getSignature().toString(), ok, completed);
					}
					if (completed) {
						if (redisTemplate.hasKey(transaction.getXaId())) {
							List<String> transactionIds = redisTemplate.opsForList().range(transaction.getXaId(), 0, -1);
							if (CollectionUtils.isNotEmpty(transactionIds)) {
								if (log.isTraceEnabled()) {
									log.trace("Call XA transactionIds: " + transactionIds);
								}
								for (String transactionId : transactionIds) {
									redisMessageSender.sendMessage("commitment:" + transaction.getXaId() + ":" + transactionId, ok);
								}
							}
							redisTemplate.delete(transaction.getXaId());
						}
					}
					transactionManager.closeTransaction(transaction.getXaId());
					if (sessionManager != null) {
						sessionManager.reset();
					}
				}
			}
		}
	}

	private void lazyCommit(final XaTransaction transaction, final XaTransactional transactionDefinition, final Signature signature,
			final Throwable cause) {
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
				if (log.isTraceEnabled()) {
					log.trace("{}, ok? {}, completed? {}", signature.toString(), ok, completed);
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

		if (log.isTraceEnabled()) {
			log.trace("Asynchronously waiting for committment to finish this transaction.");
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
