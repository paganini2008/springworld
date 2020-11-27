package com.github.paganini2008.springdessert.cluster.utils;

import com.github.paganini2008.springdessert.cluster.http.Request;

/**
 * 
 * ApiRetryHandler
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public interface ApiRetryListener {

	default void onRetryBegin(String provider, Request request) {
	}

	default void onRetryEnd(String provider, Request request, Throwable e) {
	}

	default void onEachRetry(String provider, Request request, Throwable e) {
	}

	default boolean matches(String provider, Request request) {
		return true;
	}

}
