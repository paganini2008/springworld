package com.github.paganini2008.springworld.xmemcached;

import net.rubyeye.xmemcached.MemcachedClient;

/**
 * 
 * MemcachedOperations
 *
 * @author Fred Feng
 * @version 1.0
 */
public interface MemcachedOperations {

	default boolean set(String key, Object value) throws Exception {
		return set(key, 0, value);
	}

	boolean set(String key, int expiration, Object value) throws Exception;

	<T> T get(String key, Class<T> requiredType) throws Exception;

	boolean delete(String key) throws Exception;

	boolean push(String key, int expiration, Object value) throws Exception;

	<T> T pop(String key, Class<T> requiredType) throws Exception;

	MemcachedClient getClient();

}