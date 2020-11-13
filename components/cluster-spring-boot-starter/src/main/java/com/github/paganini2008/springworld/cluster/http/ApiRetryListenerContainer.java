package com.github.paganini2008.springworld.cluster.http;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;

/**
 * 
 * ApiRetryListenerContainer
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class ApiRetryListenerContainer implements RetryListener, BeanPostProcessor {

	private final List<ApiRetryListener> listeners = new CopyOnWriteArrayList<ApiRetryListener>();

	public void addListener(ApiRetryListener listener) {
		if (listener != null) {
			listeners.add(listener);
		}
	}

	public void removeListener(ApiRetryListener listener) {
		if (listener != null) {
			listeners.remove(listener);
		}
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		if (bean instanceof ApiRetryListener) {
			addListener((ApiRetryListener) bean);
		}
		return bean;
	}

	@Override
	public <T, E extends Throwable> boolean open(RetryContext context, RetryCallback<T, E> callback) {
		final String provider = (String) context.getAttribute(RequestProcessor.CURRENT_PROVIDER_IDENTIFIER);
		final Request request = (Request) context.getAttribute(RequestProcessor.CURRENT_REQUEST_IDENTIFIER);
		listeners.forEach(listener -> {
			if (listener.matches(provider, request)) {
				listener.onRetryBegin(provider, request);
			}
		});
		return true;
	}

	@Override
	public <T, E extends Throwable> void close(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
		final String provider = (String) context.getAttribute(RequestProcessor.CURRENT_PROVIDER_IDENTIFIER);
		final Request request = (Request) context.getAttribute(RequestProcessor.CURRENT_REQUEST_IDENTIFIER);
		listeners.forEach(listener -> {
			if (listener.matches(provider, request)) {
				listener.onRetryEnd(provider, request, throwable);
			}
		});
	}

	@Override
	public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
		final String provider = (String) context.getAttribute(RequestProcessor.CURRENT_PROVIDER_IDENTIFIER);
		final Request request = (Request) context.getAttribute(RequestProcessor.CURRENT_REQUEST_IDENTIFIER);
		listeners.forEach(listener -> {
			if (listener.matches(provider, request)) {
				listener.onEachRetry(provider, request, throwable);
			}
		});
	}

}
