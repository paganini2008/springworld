package com.github.paganini2008.springdessert.cluster.http;

import java.lang.reflect.Type;

import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;

import com.github.paganini2008.springdessert.cluster.http.Statistic.Permit;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * RequestTemplate
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
@Slf4j
public class RequestTemplate {

	private final RequestProcessor requestProcessor;
	private final StatisticIndicator statisticIndicator;
	private final RequestInterceptorContainer requestInterceptorContainer;

	public RequestTemplate(RoutingAllocator routingAllocator, RestClientPerformer restClientPerformer,
			RetryTemplateFactory retryTemplateFactory, AsyncTaskExecutor taskExecutor,
			RequestInterceptorContainer requestInterceptorContainer, StatisticIndicator statisticIndicator) {
		this(new DefaultRequestProcessor(routingAllocator, restClientPerformer, retryTemplateFactory, taskExecutor),
				requestInterceptorContainer, statisticIndicator);
	}

	public RequestTemplate(RequestProcessor requestProcessor, RequestInterceptorContainer requestInterceptorContainer,
			StatisticIndicator statisticIndicator) {
		this.requestInterceptorContainer = requestInterceptorContainer;
		this.requestProcessor = requestProcessor;
		this.statisticIndicator = statisticIndicator;
	}

	public <T> ResponseEntity<T> sendRequest(String provider, Request req, Type responseType) {
		ResponseEntity<T> responseEntity = null;
		RestClientException reason = null;
		final ForwardedRequest request = (ForwardedRequest) req;
		int retries = request.getRetries();
		int timeout = request.getTimeout();
		FallbackProvider fallbackProvider = request.getFallback();

		Statistic statistic = statisticIndicator.compute(provider, request);
		Permit permit = statistic.getPermit();
		try {
			if (permit.getAvailablePermits() < 1) {
				throw new RestfulException(request, InterruptedType.TOO_MANY_REQUESTS);
			}
			permit.accquire();
			if (requestInterceptorContainer.beforeSubmit(provider, request)) {
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
			requestInterceptorContainer.afterSubmit(provider, request, responseEntity, reason);
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

}
