package com.github.paganini2008.springworld.xa;

import java.util.Map;
import java.util.UUID;
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
 * LocalXaTransactionManager
 *
 * @author Fred Feng
 * @version 1.0
 */
@Slf4j
public class LocalXaTransactionManager implements XaTransactionManager {

	private final Map<String, XaTransaction> cache = new ConcurrentHashMap<String, XaTransaction>();

	@Autowired
	private XaTransactionFactory xaTransactionFactory;

	@Override
	public XaTransaction openTransaction() {
		final String xaId = getXaId();
		XaTransaction transaction = MapUtils.get(cache, xaId, () -> {
			return xaTransactionFactory.createTransaction(xaId);
		});
		if (log.isTraceEnabled()) {
			log.trace("Current transaction: " + transaction.toString());
		}
		return transaction;
	}

	@Override
	public void closeTransaction(String xaId) {
		XaTransaction transaction = cache.get(xaId);
		if (transaction != null) {
			try {
				if (!transaction.isCompleted()) {
					transaction.rollback();
				}
			} finally {
				cache.remove(xaId);
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
			xaId = createXaId();
		}
		httpServletRequest.setAttribute(XA_HTTP_REQUEST_IDENTITY, xaId);
		return xaId;
	}

	protected String createXaId() {
		return "XA-" + UUID.randomUUID().toString().replace("-", "");
	}

}
