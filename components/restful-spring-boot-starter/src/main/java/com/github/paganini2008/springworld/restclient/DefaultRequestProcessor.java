package com.github.paganini2008.springworld.restclient;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;

import com.github.paganini2008.devtools.Comparables;
import com.github.paganini2008.devtools.StringUtils;
import com.github.paganini2008.devtools.collection.MapUtils;
import com.github.paganini2008.devtools.date.DateUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * DefaultRequestProcessor
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Slf4j
public class DefaultRequestProcessor implements RequestProcessor {

	private final String provider;
	private final int defaultRetries;
	private final int defaultTimeout;
	private final @Nullable MultiValueMap<String, String> defaultHttpHeaders;
	private final RoutingPolicy routingPolicy;
	private final EnhancedRestTemplate restTemplate;
	private final ThreadPoolTaskExecutor taskExecutor;

	DefaultRequestProcessor(String provider, int retries, int timeout, MultiValueMap<String, String> headers, RoutingPolicy routingPolicy,
			EnhancedRestTemplate restTemplate, ThreadPoolTaskExecutor taskExecutor) {
		this.provider = provider;
		this.defaultRetries = retries;
		this.defaultTimeout = timeout;
		this.defaultHttpHeaders = headers;
		this.routingPolicy = routingPolicy;
		this.restTemplate = restTemplate;
		this.taskExecutor = taskExecutor;
	}

	protected RetryTemplate createRetryTemplate(String provider, String path, int retries) {
		return new RetryTemplateBuilder().setRetryPolicy(retries).build();
	}

	@Override
	public <T> ResponseEntity<T> sendRequestWithRetry(Request request, Type responseType, int retries) {
		RetryTemplate retryTemplate = createRetryTemplate(provider, request.getPath(),
				Comparables.getOrDefault(retries, 0, defaultRetries));
		return retryTemplate.execute(context -> {
			return sendRequest(request, responseType);
		}, context -> {
			Throwable e = context.getLastThrowable();
			if (e instanceof RestClientException) {
				throw (RestClientException) e;
			}
			throw new RestfulApiException(e.getMessage(), e);
		});

	}

	@Override
	public <T> ResponseEntity<T> sendRequest(Request request, Type responseType) {
		String path = request.getPath();
		Map<String, Object> pathVariables = request.getPathVariables();
		if (MapUtils.isNotEmpty(pathVariables)) {
			path = StringUtils.parseText(path, "{", "}", pathVariables);
		}
		String url = routingPolicy.extractUrl(provider, path);
		HttpEntity<?> body = request.getBody();
		if (MapUtils.isNotEmpty(defaultHttpHeaders)) {
			body.getHeaders().addAll(defaultHttpHeaders);
		}
		printFoot(url, request);
		return restTemplate.perform(url, request.getMethod(), body, responseType, request.getRequestParameters());
	}

	@Override
	public <T> ResponseEntity<T> sendRequestWithTimeout(Request request, Type responseType, int timeout) {
		Future<ResponseEntity<T>> future = taskExecutor.submit(() -> {
			return sendRequest(request, responseType);
		});
		timeout = Comparables.getOrDefault(timeout, -1, defaultTimeout);
		try {
			if (timeout > 0) {
				return future.get(timeout, TimeUnit.SECONDS);
			}
			return future.get();
		} catch (InterruptedException | CancellationException | TimeoutException e) {
			throw new RestfulApiException(e.getMessage(), e);
		} catch (ExecutionException e) {
			Throwable real = e.getCause();
			if (real instanceof RestClientException) {
				throw (RestClientException) real;
			}
			throw new RestfulApiException(real.getMessage(), real);
		}
	}

	@Override
	public <T> ResponseEntity<T> sendRequestWithRetryAndTimeout(Request request, Type responseType, int retries, int timeout) {
		Future<ResponseEntity<T>> future = taskExecutor.submit(() -> {
			return sendRequestWithRetry(request, responseType, retries);
		});
		timeout = Comparables.getOrDefault(timeout, -1, defaultTimeout);
		try {
			if (timeout > 0) {
				return future.get(timeout, TimeUnit.SECONDS);
			}
			return future.get();
		} catch (InterruptedException | CancellationException | TimeoutException e) {
			throw new RestfulApiException(e.getMessage(), e);
		} catch (ExecutionException e) {
			Throwable real = e.getCause();
			if (real instanceof RestClientException) {
				throw (RestClientException) real;
			}
			throw new RestfulApiException(real.getMessage(), real);
		}
	}

	private void printFoot(String url, Request request) {
		if (log.isTraceEnabled()) {
			log.trace("<RestClient path: {}>", url);
			log.trace("<RestClient use method: {}>", request.getMethod());
			log.trace("<RestClient request headers: {}>", request.getHeaders());
			log.trace("<RestClient request parameters: {}>", request.getRequestParameters());
			log.trace("<RestClient path variables: {}>", request.getPathVariables());
			log.trace("<RestClient date: {}>", DateUtils.format(request.getTimestamp(), "MM/dd/yy HH:mm:ss"));
		}
	}
}
