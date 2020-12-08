package com.github.paganini2008.springdessert.gateway;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.github.paganini2008.devtools.collection.MapUtils;
import com.github.paganini2008.devtools.io.PathUtils;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 
 * Route
 * @author Fred Feng
 *
 * @since 1.0
 */
@Accessors(fluent = true)
@Data
public final class Route implements Comparable<Route> {

	private final String prefix;
	private final int prefixEndPosition;
	private String provider;
	private int retries;
	private int timeout;
	private int concurrency;
	private boolean direct;
	private Class<?> fallbackClass;
	private HttpStatus[] fallbackHttpStatus;
	private Class<? super Throwable>[] fallbackException;
	private final MultiValueMap<String, String> defaultHeaders = new LinkedMultiValueMap<String, String>();
	private final List<String> ignoredHeaders = new ArrayList<String>();

	Route(String prefix) {
		this.prefix = prefix;
		this.prefixEndPosition = PathUtils.indexOfLastSeparator(prefix);
	}

	public Route ignoredHeaders(String[] headerNames) {
		this.ignoredHeaders.addAll(Arrays.asList(headerNames));
		return this;
	}

	public Route defaultHeaders(String[] nameValues) {
		Map<String, String> headerMap = MapUtils.toMap(nameValues);
		for (Map.Entry<String, String> entry : headerMap.entrySet()) {
			this.defaultHeaders.add(entry.getKey(), entry.getValue());
		}
		return this;
	}

	@Override
	public int compareTo(Route other) {
		return other.prefixEndPosition() - prefixEndPosition();
	}

}
