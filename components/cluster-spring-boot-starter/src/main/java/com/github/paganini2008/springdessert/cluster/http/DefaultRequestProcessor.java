package com.github.paganini2008.springdessert.cluster.http;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

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
 * @author Jimmy Hoff
 *
 * @since 1.0
 */
@Slf4j
public class DefaultRequestProcessor implements RequestProcessor {

	private static final int retryTemplateCacheSize = 256;

	private static final MultiMappedMap<String, String, RetryTemplate> retryTemplateCache = new MultiMappedMap<>(() -> {
		return new LruMap<String, RetryTemplate>(retryTemplateCacheSize);
	});

	private final String provider;
	private final @Nullable MultiValueMap<String, String> defaultHttpHeaders;
	private final RoutingAllocator routingAllocator;
	private final EnhancedRestTemplate restTemplate;
	private final RetryTemplateFactory retryTemplateFactory;
	private final ThreadPoolTaskExecutor taskExecutor;

	DefaultRequestProcessor(String provider, MultiValueMap<String, String> headers, RoutingAllocator routingAllocator,
			EnhancedRestTemplate restTemplate, RetryTemplateFactory retryTemplateFactory, ThreadPoolTaskExecutor taskExecutor) {
		this.provider = provider;
		this.defaultHttpHeaders = headers;
		this.routingAllocator = routingAllocator;
		this.restTemplate = restTemplate;
		this.retryTemplateFactory = retryTemplateFactory;
		this.taskExecutor = taskExecutor;
	}

	private final MultiMappedMap<String, String, AtomicInteger> concurrencies = new MultiMappedMap<String, String, AtomicInteger>();

	@Override
	public <T> ResponseEntity<T> sendRequestWithRetry(Request request, Type responseType, int maxConcurrency, int retries) {
		RetryTemplate retryTemplate = retryTemplateCache.get(provider, request.getPath(), () -> {
			return retryTemplateFactory.setRetryPolicy(retries).createObject();
		});
		RetryEntry retryEntry = new RetryEntry(provider, request, retries);
		return retryTemplate.execute(context -> {
			context.setAttribute(CURRENT_RETRY_IDENTIFIER, retryEntry);
			return sendRequest(request, responseType, maxConcurrency);
		}, context -> {
			context.removeAttribute(CURRENT_RETRY_IDENTIFIER);
			Throwable e = context.getLastThrowable();
			if (e instanceof RestClientException) {
				throw (RestClientException) e;
			}
			throw new RestfulException(e.getMessage(), e, request);
		});

	}

	@Override
	public <T> ResponseEntity<T> sendRequest(Request request, Type responseType, int maxConcurrency) {
		Map<String, Object> uriVariables = new HashMap<String, Object>();
		String path = request.getPath();
		if (MapUtils.isNotEmpty(request.getPathVariables())) {
			uriVariables.putAll(request.getPathVariables());
		}
		StringBuilder url = new StringBuilder(routingAllocator.allocateHost(provider, path));
		if (MapUtils.isNotEmpty(request.getRequestParameters())) {
			url.append("?").append(getQueryString(request.getRequestParameters()));
			uriVariables.putAll(request.getRequestParameters());
		}
		HttpEntity<?> body = request.getBody();
		if (MapUtils.isNotEmpty(defaultHttpHeaders)) {
			body.getHeaders().addAll(defaultHttpHeaders);
		}
		int concurrency = getConcurrency(request).incrementAndGet();
		try {
			if (concurrency > maxConcurrency) {
				throw new TooManyRequestsException(request);
			}
			printFoot(url.toString(), request);
			return restTemplate.perform(url.toString(), request.getMethod(), body, responseType, uriVariables);
		} finally {
			getConcurrency(request).decrementAndGet();
		}
	}

	private AtomicInteger getConcurrency(Request request) {
		return concurrencies.get(provider, request.getPath(), () -> {
			return new AtomicInteger(0);
		});
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
	public <T> ResponseEntity<T> sendRequestWithTimeout(Request request, Type responseType, int maxConcurrency, int timeout) {
		Future<ResponseEntity<T>> future = taskExecutor.submit(() -> {
			return sendRequest(request, responseType, maxConcurrency);
		});
		try {
			if (timeout > 0) {
				return future.get(timeout, TimeUnit.SECONDS);
			}
			return future.get();
		} catch (TimeoutException e) {
			throw new RequestTimeoutException(request);
		} catch (InterruptedException | CancellationException e) {
			throw new RestfulException(e.getMessage(), e, request);
		} catch (ExecutionException e) {
			Throwable real = e.getCause();
			if (real instanceof RestClientException) {
				throw (RestClientException) real;
			}
			throw new RestfulException(real.getMessage(), real, request);
		} catch (Throwable e) {
			throw new RestfulException(e.getMessage(), e, request);
		}
	}

	@Override
	public <T> ResponseEntity<T> sendRequestWithRetryAndTimeout(Request request, Type responseType, int maxConcurrency, int retries,
			int timeout) {
		Future<ResponseEntity<T>> future = taskExecutor.submit(() -> {
			return sendRequestWithRetry(request, responseType, maxConcurrency, retries);
		});
		try {
			if (timeout > 0) {
				return future.get(timeout, TimeUnit.SECONDS);
			}
			return future.get();
		} catch (TimeoutException e) {
			throw new RequestTimeoutException(request);
		} catch (InterruptedException | CancellationException e) {
			throw new RestfulException(e.getMessage(), e, request);
		} catch (ExecutionException e) {
			Throwable real = e.getCause();
			if (real instanceof RestClientException) {
				throw (RestClientException) real;
			}
			throw new RestfulException(real.getMessage(), real, request);
		} catch (Throwable e) {
			throw new RestfulException(e.getMessage(), e, request);
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

	@Getter
	@Setter
	static class RetryEntry {

		private String provider;
		private Request request;
		private int maxAttempts;

		RetryEntry(String provider, Request request, int maxAttempts) {
			this.provider = provider;
			this.request = request;
			this.maxAttempts = maxAttempts;
		}

	}
}
