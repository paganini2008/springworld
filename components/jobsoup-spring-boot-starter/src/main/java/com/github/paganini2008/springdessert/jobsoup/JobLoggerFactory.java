package com.github.paganini2008.springdessert.jobsoup;

import org.slf4j.Logger;

import com.github.paganini2008.devtools.collection.LruMap;
import com.github.paganini2008.devtools.proxy.Aspect;
import com.github.paganini2008.devtools.proxy.JdkProxyFactory;
import com.github.paganini2008.devtools.proxy.ProxyFactory;

import lombok.Getter;

/**
 * 
 * JobLoggerFactory
 * 
 * @author Jimmy Hoff
 *
 * @since 1.0
 */
public final class JobLoggerFactory {

	private static final ProxyFactory proxyFactory = new JdkProxyFactory();
	private static final LruMap<JobKey, LoggerProxy> proxies = new LruMap<JobKey, LoggerProxy>(1024);

	public static Logger getLogger(Logger target, long traceId, JobKey jobKey, LogManager logManager) {
		LoggerProxy holder = proxies.get(jobKey);
		if (holder == null) {
			proxies.put(jobKey, createLoggerProxy(target, jobKey, logManager));
			holder = proxies.get(jobKey);
		}
		((JobLoggerAspect) holder.getAspect()).setTraceId(traceId);
		return holder.getProxy();
	}

	private static LoggerProxy createLoggerProxy(Logger target, JobKey jobKey, LogManager logManager) {
		Aspect aspect = new JobLoggerAspect(jobKey, logManager);
		Logger proxy = (Logger) proxyFactory.getProxy(target, aspect, Logger.class);
		return new LoggerProxy(proxy, aspect);
	}

	@Getter
	private static class LoggerProxy {

		private final Logger proxy;
		private final Aspect aspect;

		LoggerProxy(Logger proxy, Aspect aspect) {
			this.proxy = proxy;
			this.aspect = aspect;
		}

	}

}
