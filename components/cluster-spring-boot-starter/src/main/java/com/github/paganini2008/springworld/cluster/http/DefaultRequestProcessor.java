package com.github.paganini2008.springworld.cluster.http;

import java.lang.reflect.Type;
import java.util.HashMap;
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

import com.github.paganini2008.devtools.collection.LruMap;
import com.github.paganini2008.devtools.collection.MapUtils;
import com.github.paganini2008.devtools.collection.MultiMappedMap;
import com.github.paganini2008.devtools.date.DateUtils;

import lombok.Getter;
import lombok.Setter;
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
	private final @Nullable MultiValueMap<String, String> defaultHttpHeaders;
	private final RoutingPolicy routingPolicy;
	private final EnhancedRestTemplate restTemplate;
	private final RetryTemplateFactory retryTemplateFactory;
	private final ThreadPoolTaskExecutor taskExecutor;

	DefaultRequestProcessor(String provider, MultiValueMap<String, String> headers, RoutingPolicy routingPolicy,
			EnhancedRestTemplate restTemplate, RetryTemplateFactory retryTemplateFactory, ThreadPoolTaskExecutor taskExecutor) {
		this.provider = provider;
		this.defaultHttpHeaders = headers;
		this.routingPolicy = routingPolicy;
		this.restTemplate = restTemplate;
		this.retryTemplateFactory = retryTemplateFactory;
		this.taskExecutor = taskExecutor;
	}

	private MultiMappedMap<String, String, RetryTemplate> retryTemplateCache = new MultiMappedMap<>(() -> {
		return new LruMap<String, RetryTemplate>(256);
	});

	@Override
	public <T> ResponseEntity<T> sendRequestWithRetry(Request request, Type responseType, int retries) {
		RetryTemplate retryTemplate = retryTemplateCache.get(provider, request.getPath(), () -> {
			return retryTemplateFactory.setRetryPolicy(retries).createObject();
		});
		RetryEntry retryEntry = new RetryEntry(provider, request, retries);
		return retryTemplate.execute(context -> {
			context.setAttribute(CURRENT_RETRY_IDENTIFIER, retryEntry);
			return sendRequest(request, responseType);
		}, context -> {
			context.removeAttribute(CURRENT_RETRY_IDENTIFIER);
			Throwable e = context.getLastThrowable();
			if (e instanceof RestClientException) {
				throw (RestClientException) e;
			}
			throw new RestfulApiException(e.getMessage(), e);
		});

	}

	@Getter
	@Setter
	static class RetryEntry {

		private String provider;
		private Request request;
		private int retries;

		RetryEntry(String provider, Request request, int retries) {
			this.provider = provider;
			this.request = request;
			this.retries = retries;
		}

	}

	@Override
	public <T> ResponseEntity<T> sendRequest(Request request, Type responseType) {
		Map<String, Object> uriVariables = new HashMap<String, Object>();
		String path = request.getPath();
		if (MapUtils.isNotEmpty(request.getPathVariables())) {
			uriVariables.putAll(request.getPathVariables());
		}
		String url = routingPolicy.extractUrl(provider, path);
		if (MapUtils.isNotEmpty(request.getRequestParameters())) {
			url += "?" + getQueryString(request.getRequestParameters());
			uriVariables.putAll(request.getRequestParameters());
		}
		HttpEntity<?> body = request.getBody();
		if (MapUtils.isNotEmpty(defaultHttpHeaders)) {
			body.getHeaders().addAll(defaultHttpHeaders);
		}
		printFoot(url, request);
		return restTemplate.perform(url, request.getMethod(), body, responseType, uriVariables);
	}

	private String getQueryString(Map<String, Object> queryMap) {
		StringBuilder str = new StringBuilder();
		String[] names = queryMap.keySet().toArray(new String[0]);
		for (int i = 0, l = names.length; i < l; i++) {
			str.append(names[i]).append("={").append(names[i]).append("}");
			if (i != l - 1) {
				str.append("&");
			}
		}
		return str.toString();
	}

	@Override
	public <T> ResponseEntity<T> sendRequestWithTimeout(Request request, Type responseType, int timeout) {
		Future<ResponseEntity<T>> future = taskExecutor.submit(() -> {
			return sendRequest(request, responseType);
		});
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
