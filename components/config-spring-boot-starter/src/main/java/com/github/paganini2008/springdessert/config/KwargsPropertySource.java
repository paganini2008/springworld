package com.github.paganini2008.springdessert.config;

import java.util.Map;

import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

/**
 * 
 * KwargsPropertySource
 * 
 * @author Jimmy Hoff
 *
 * @since 1.0
 */
public class KwargsPropertySource extends EnumerablePropertySource<Map<String, String>> {

	public KwargsPropertySource(String name, Map<String, String> source) {
		super(name, source);
	}

	@Override
	@Nullable
	public Object getProperty(String name) {
		return this.source.get(name);
	}

	@Override
	public boolean containsProperty(String name) {
		return this.source.containsKey(name);
	}

	@Override
	public String[] getPropertyNames() {
		return StringUtils.toStringArray(this.source.keySet());
	}
}
