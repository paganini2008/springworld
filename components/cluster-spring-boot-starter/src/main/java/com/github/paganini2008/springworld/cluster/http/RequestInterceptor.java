package com.github.paganini2008.springworld.cluster.http;

import org.springframework.http.ResponseEntity;

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

	void afterSubmit(Request request, ResponseEntity<?> responseEntity, Throwable reason);

	default boolean matches(Request request) {
		return true;
	}

}
