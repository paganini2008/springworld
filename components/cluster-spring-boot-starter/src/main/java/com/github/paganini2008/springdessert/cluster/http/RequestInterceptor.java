package com.github.paganini2008.springdessert.cluster.http;

import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;

/**
 * 
 * RequestInterceptor
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public interface RequestInterceptor {

	void beforeSubmit(Request request);

	void afterSubmit(Request request, @Nullable ResponseEntity<?> responseEntity, Throwable reason);

	default boolean matches(Request request) {
		return true;
	}

}
