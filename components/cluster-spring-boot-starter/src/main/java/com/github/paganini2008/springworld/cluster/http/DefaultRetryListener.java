package com.github.paganini2008.springworld.cluster.http;

import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * DefaultRetryListener
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Slf4j
public class DefaultRetryListener implements RetryListener {

	@Override
	public <T, E extends Throwable> boolean open(RetryContext context, RetryCallback<T, E> callback) {
		return true;
	}

	@Override
	public <T, E extends Throwable> void close(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
	}

	@Override
	public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
		if (log.isInfoEnabled()) {
			Request request = (Request) context.getAttribute(RequestProcessor.CURRENT_REQUEST_IDENTIFIER);
			log.info("Retry: {}, Times: {}", request, context.getRetryCount());
		}
	}

}
