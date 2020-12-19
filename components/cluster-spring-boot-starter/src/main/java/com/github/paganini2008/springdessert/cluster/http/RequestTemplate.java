package com.github.paganini2008.springdessert.cluster.http;

import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;

import com.github.paganini2008.springdessert.cluster.http.StatisticMetric.Permit;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * RequestTemplate
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
@Slf4j
public class RequestTemplate implements BeanPostProcessor {

	private final List<RequestInterceptor> interceptors = new CopyOnWriteArrayList<RequestInterceptor>();
	private final RequestProcessor requestProcessor;
	private final StatisticIndicator statisticIndicator;

	public RequestTemplate(RoutingAllocator routingAllocator, RestClientPerformer restClientPerformer,
			RetryTemplateFactory retryTemplateFactory, AsyncTaskExecutor taskExecutor, StatisticIndicator statisticIndicator) {
		this(new DefaultRequestProcessor(routingAllocator, restClientPerformer, retryTemplateFactory, taskExecutor), statisticIndicator);
	}

	public RequestTemplate(RequestProcessor requestProcessor, StatisticIndicator statisticIndicator) {
		this.requestProcessor = requestProcessor;
		this.statisticIndicator = statisticIndicator;
	}

	public <T> ResponseEntity<T> sendRequest(String provider, Request req, Type responseType) {
		ResponseEntity<T> responseEntity = null;
		RestClientException reason = null;
		final ForwardedRequest request = (ForwardedRequest) req;
		final String path = request.getPath();
		int retries = request.getRetries();
		int timeout = request.getTimeout();
		FallbackProvider fallbackProvider = request.getFallback();

		Statistic statistic = statisticIndicator.getStatistic(provider, path);
		Permit permit = statistic.getPermit();
		try {
			if (permit.availablePermits() < 1) {
				throw new RestfulException(request, InterruptedType.TOO_MANY_REQUESTS);
			}
			permit.accquire();
			if (beforeSubmit(provider, request)) {
				if (retries > 0 && timeout > 0) {
					responseEntity = requestProcessor.sendRequestWithRetryAndTimeout(provider, request, responseType, retries, timeout);
				} else if (retries < 1 && timeout > 0) {
					responseEntity = requestProcessor.sendRequestWithTimeout(provider, request, responseType, timeout);
				} else if (retries > 0 && timeout < 1) {
					responseEntity = requestProcessor.sendRequestWithRetry(provider, request, responseType, retries);
				} else {
					responseEntity = requestProcessor.sendRequest(provider, request, responseType);
				}
			}
		} catch (RestClientException e) {
			log.error(e.getMessage(), e);
			responseEntity = executeFallback(provider, request, responseType, e, fallbackProvider);
			reason = e;
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
		} finally {
			permit.release();
			afterSubmit(provider, request, responseEntity, reason);
		}
		if (responseEntity == null) {
			responseEntity = executeFallback(provider, request, responseType, reason, fallbackProvider);
		}
		return responseEntity;
	}

	@SuppressWarnings("unchecked")
	protected <T> ResponseEntity<T> executeFallback(String provider, Request request, Type responseType, RestClientException e,
			FallbackProvider fallback) {
		if (fallback == null) {
			throw e;
		}
		try {
			if (fallback.hasFallback(provider, request, responseType, e)) {
				T body = (T) fallback.getBody(provider, request, responseType, e);
				return new ResponseEntity<T>(body, fallback.getHeaders(), fallback.getHttpStatus());
			}
		} catch (Exception fallbackError) {
			throw RestClientUtils.wrapException("Failed to execute fallback", fallbackError, request);
		}
		throw e;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		if (bean instanceof RequestInterceptor) {
			addInterceptor((RequestInterceptor) bean);
		}
		return bean;
	}

	public void addInterceptor(RequestInterceptor interceptor) {
		if (interceptor != null) {
			interceptors.add(interceptor);
		}
	}

	public void removeInterceptor(RequestInterceptor interceptor) {
		if (interceptor != null) {
			interceptors.remove(interceptor);
		}
	}

	public boolean beforeSubmit(String provider, Request request) {
		boolean proceeded = true;
		for (RequestInterceptor interceptor : interceptors) {
			if (interceptor.matches(provider, request)) {
				proceeded &= interceptor.beforeSubmit(provider, request);
			}
		}
		return proceeded;
	}

	public void afterSubmit(String provider, Request request, ResponseEntity<?> responseEntity, Throwable reason) {
		for (RequestInterceptor interceptor : interceptors) {
			if (interceptor.matches(provider, request)) {
				interceptor.afterSubmit(provider, request, responseEntity, reason);
			}
		}
	}

}
