package com.github.paganini2008.springdessert.cluster.http;

import java.lang.reflect.Method;

import org.springframework.web.client.RestClientException;

/**
 * 
 * NullableFallbackProvider
 *
 * @author Jimmy Hoff
 * 
 * @since 1.0
 */
public class DefaultFallbackProvider implements FallbackProvider {

	@Override
	public Object getBody(String provider, Method method, Object[] arguments, RestClientException e) {
		return null;
	}

}
