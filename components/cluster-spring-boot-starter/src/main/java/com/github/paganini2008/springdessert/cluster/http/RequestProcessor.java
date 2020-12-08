package com.github.paganini2008.springdessert.cluster.http;

import java.lang.reflect.Type;

import org.springframework.http.ResponseEntity;

/**
 * 
 * RequestProcessor
 * 
 * @author Jimmy Hoff
 *
 * @since 1.0
 */
public interface RequestProcessor {

	static final String CURRENT_RETRY_IDENTIFIER = "current-retry";

	<T> ResponseEntity<T> sendRequestWithRetry(Request request, Type responseType, int maxConcurrency, int retries);

	<T> ResponseEntity<T> sendRequest(Request request, Type responseType, int maxConcurrency);

	<T> ResponseEntity<T> sendRequestWithTimeout(Request request, Type responseType, int maxConcurrency, int timeout);

	<T> ResponseEntity<T> sendRequestWithRetryAndTimeout(Request request, Type responseType, int maxConcurrency, int retries, int timeout);
}
