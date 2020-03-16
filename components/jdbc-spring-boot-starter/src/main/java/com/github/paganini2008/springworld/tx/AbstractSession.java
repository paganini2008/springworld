package com.github.paganini2008.springworld.tx;

import com.github.paganini2008.devtools.cache.Cache;
import com.github.paganini2008.devtools.db4j.JdbcOperations;

/**
 * 
 * AbstractSession
 *
 * @author Fred Feng
 * @version 1.0
 */
public abstract class AbstractSession implements Session {

	protected AbstractSession(JdbcOperations jdbcOperations, Cache cache) {
		this.jdbcOperations = jdbcOperations;
		this.cache = cache;
	}

	private final JdbcOperations jdbcOperations;
	private final Cache cache;

	@Override
	public JdbcOperations getJdbcOperations() {
		return jdbcOperations;
	}

	@Override
	public long getTimeout() {
		return -1L;
	}

	@Override
	public Object cache(String cacheKey) {
		checkCacheEnabled();
		return cache.getObject(cacheKey);
	}

	@Override
	public void cache(String cacheKey, Object cachedValue) {
		checkCacheEnabled();
		cache.putObject(cacheKey, cachedValue);
	}

	private void checkCacheEnabled() {
		if (cache == null) {
			throw new IllegalStateException("Session cache is disabled.");
		}
	}

}
