package com.github.paganini2008.springworld.xa;

import static com.github.paganini2008.springworld.xa.XaTransactionManager.XA_HTTP_REQUEST_IDENTITY;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.github.paganini2008.devtools.collection.CollectionUtils;
import com.github.paganini2008.devtools.multithreads.ThreadLocalInteger;
import com.github.paganini2008.springworld.redis.pubsub.RedisMessageSender;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * XaTransactionalProcessor
 *
 * @author Fred Feng
 * @version 1.0
 */
@Slf4j
@Aspect
public class XaTransactionalProcessor {

	@Autowired
	private XaTransactionManager transactionManager;

	@Autowired
	private RedisMessageSender redisMessageSender;

	@Autowired
	private StringRedisTemplate redisTemplate;

	private final ThreadLocalInteger nestable = new ThreadLocalInteger(0);

	@Pointcut("execution(public * *(..))")
	public void signature() {
	}

	@Around("signature() && @annotation(com.github.paganini2008.springworld.xa.XaTransactional)")
	public Object arround(ProceedingJoinPoint pjp) throws Throwable {
		boolean ok = true;
		XaTransaction transaction = transactionManager.openTransaction();
		nestable.incrementAndGet();
		try {
			return pjp.proceed();
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			ok = false;
			throw e;
		} finally {
			XaTransactionResponse response;
			if (hasXaHeader()) {
				if (!isNestable()) {
					redisTemplate.opsForList().rightPush(transaction.getXaId(), transaction.getId());

					XaTransactionCommitment commitment = new XaTransactionCommitment(transaction, transactionManager, redisMessageSender);
					redisMessageSender.subscribeChannel(transaction.getId(), commitment);
				}
			} else {
				if (!isNestable()) {
					if (ok) {
						response = transaction.commit();
					} else {
						response = transaction.rollback();
					}
					if (response.isCompleted()) {
						if (redisTemplate.hasKey(transaction.getXaId())) {
							List<String> transactionIds = redisTemplate.opsForList().range(transaction.getXaId(), 0, -1);
							if (CollectionUtils.isNotCollection(transactionIds)) {
								for (String transactionId : transactionIds) {
									redisMessageSender.sendMessage("commitment:" + transaction.getXaId() + ":" + transactionId, ok);
								}
							}
							redisTemplate.delete(transaction.getXaId());
						}
					}
					transactionManager.closeTransaction(transaction.getXaId());
				}
			}
		}
	}

	protected boolean hasXaHeader() {
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

}
