package com.github.paganini2008.springworld.cluster.http;

import java.lang.reflect.Type;

import org.springframework.http.ResponseEntity;

/**
 * 
 * RequestProcessor
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public interface RequestProcessor {

	static final String CURRENT_RETRY_IDENTIFIER = "current-retry";

	<T> ResponseEntity<T> sendRequestWithRetry(Request request, Type responseType, int retries);

	<T> ResponseEntity<T> sendRequest(Request request, Type responseType);

	<T> ResponseEntity<T> sendRequestWithTimeout(Request request, Type responseType, int timeout);

	<T> ResponseEntity<T> sendRequestWithRetryAndTimeout(Request request, Type responseType, int retries, int timeout);
}
