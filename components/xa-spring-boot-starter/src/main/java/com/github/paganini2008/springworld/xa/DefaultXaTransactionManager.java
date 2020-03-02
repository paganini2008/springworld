package com.github.paganini2008.springworld.xa;

import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.github.paganini2008.devtools.StringUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * DefaultXaTransactionManager
 *
 * @author Fred Feng
 * @version 1.0
 */
@Slf4j
public class DefaultXaTransactionManager extends ThreadLocal<XaTransaction> implements XaTransactionManager {

	@Autowired
	private PlatformTransactionManager transactionManager;

	@Autowired
	private StringRedisTemplate redisTemplate;

	@Override
	public XaTransaction openTransaction() {
		XaTransaction transaction = get();
		if (log.isTraceEnabled()) {
			log.trace("Open transaction: " + transaction.toString());
		}
		return transaction;
	}

	@Override
	public void closeTransaction() {
		XaTransaction transaction = get();
		if (transaction.isCompleted()) {
			redisTemplate.delete(transaction.getXaId());
			remove();
			if (log.isTraceEnabled()) {
				log.trace("Close transaction: " + transaction.toString());
			}
		}
	}

	@Override
	public void set(XaTransaction transaction) {
		throw new UnsupportedOperationException();
	}

	@Override
	protected final XaTransaction initialValue() {
		DefaultTransactionDefinition transactionDefinition = new DefaultTransactionDefinition();
		transactionDefinition.setPropagationBehavior(DefaultTransactionDefinition.PROPAGATION_REQUIRES_NEW);
		transactionDefinition.setReadOnly(false);
		TransactionStatus transactionStatus = transactionManager.getTransaction(transactionDefinition);
		return new XaTransactionImpl(getXaId(), transactionManager, transactionStatus);
	}

	private String getXaId() {
		HttpServletRequest httpServletRequest = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
		String xaid = httpServletRequest.getParameter(XA_HTTP_REQUEST_IDENTITY);
		if (StringUtils.isBlank(xaid)) {
			xaid = httpServletRequest.getHeader(XA_HTTP_REQUEST_IDENTITY);
		}
		if (StringUtils.isBlank(xaid)) {
			xaid = createXaId();
		}
		return xaid;
	}

	protected String createXaId() {
		return "XA-" + UUID.randomUUID().toString().replace("-", "");
	}

}
