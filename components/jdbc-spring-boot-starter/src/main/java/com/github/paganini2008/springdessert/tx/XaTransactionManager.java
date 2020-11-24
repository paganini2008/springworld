package com.github.paganini2008.springdessert.tx;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.github.paganini2008.devtools.StringUtils;
import com.github.paganini2008.devtools.collection.MapUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * XaTransactionManager
 *
 * @author Fred Feng
 * @version 1.0
 */
@Slf4j
public class XaTransactionManager implements TransactionManager {

	public static final String XA_HTTP_REQUEST_IDENTITY = "xaid";

	private final Map<String, XaTransaction> holder = new ConcurrentHashMap<String, XaTransaction>();

	@Autowired
	private XaTransactionFactory xaTransactionFactory;

	@Autowired
	private IdGenerator idGenerator;

	@Autowired
	private TransactionEventPublisher transactionEventPublisher;

	@Autowired
	private TransactionEventListenerContainer eventListenerContainer;

	@Override
	public Transaction currentTransaction() {
		final String xaId = getXaId();
		XaTransaction xaTansaction = MapUtils.get(holder, xaId, () -> {
			XaTransaction newTransaction = xaTransactionFactory.newTransaction(xaId);
			if (log.isTraceEnabled()) {
				log.trace("New XaTransaction: " + newTransaction.toString());
			}
			transactionEventPublisher.afterCreate(xaId);
			return newTransaction;
		});
		return xaTansaction;
	}

	@Override
	public void registerEventListener(TransactionPhase transactionPhase, String id, TransactionEventListener eventListener) {
		eventListenerContainer.registerEventListener(transactionPhase, id, eventListener);
	}

	@Override
	public String currentTransactionId() {
		return ((XaTransaction) currentTransaction()).getXaId();
	}

	@Override
	public void closeTransaction(String xaId) {
		transactionEventPublisher.beforeClose(xaId);
		XaTransaction transaction = holder.remove(xaId);
		if (transaction != null) {
			if (!transaction.isCompleted()) {
				transaction.rollback();
			}
			if (log.isTraceEnabled()) {
				log.trace("Close transaction: " + transaction.toString());
			}
		}
	}

	private String getXaId() {
		HttpServletRequest httpServletRequest = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
		String xaId = (String) httpServletRequest.getAttribute(XA_HTTP_REQUEST_IDENTITY);
		if (StringUtils.isNotBlank(xaId)) {
			return xaId;
		}
		xaId = httpServletRequest.getParameter(XA_HTTP_REQUEST_IDENTITY);
		if (StringUtils.isBlank(xaId)) {
			xaId = httpServletRequest.getHeader(XA_HTTP_REQUEST_IDENTITY);
		}
		if (StringUtils.isBlank(xaId)) {
			xaId = idGenerator.generateXaTransactionId();
		}
		httpServletRequest.setAttribute(XA_HTTP_REQUEST_IDENTITY, xaId);
		return xaId;
	}

}
