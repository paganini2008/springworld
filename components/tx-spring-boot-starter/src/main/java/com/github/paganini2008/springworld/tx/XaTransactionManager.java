package com.github.paganini2008.springworld.tx;

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
	private TransactionFactory transactionFactory;

	@Autowired
	private TransactionId transactionId;

	@Override
	public Transaction openTransaction() {
		final String xaId = getXaId();
		XaTransaction xaTansaction = MapUtils.get(holder, xaId, () -> {
			Transaction transaction = transactionFactory.createTransaction(transactionId.generateId());
			return new XaTransactionImpl(xaId, transaction);
		});
		if (log.isTraceEnabled()) {
			log.trace("Current transaction: " + xaTansaction.toString());
		}
		return xaTansaction;
	}

	@Override
	public void closeTransaction(String xaId) {
		Transaction transaction = holder.remove(xaId);
		if (transaction != null) {
			try {
				if (!transaction.isCompleted()) {
					transaction.rollback();
				}
			} finally {
				if (log.isTraceEnabled()) {
					log.trace("Close transaction: " + transaction.toString());
				}
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
			xaId = transactionId.generateXaId();
		}
		httpServletRequest.setAttribute(XA_HTTP_REQUEST_IDENTITY, xaId);
		return xaId;
	}

}
