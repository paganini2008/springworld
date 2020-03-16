package com.github.paganini2008.springworld.xmemcached;

import net.rubyeye.xmemcached.MemcachedClient;

/**
 * 
 * MemcachedTemplate
 *
 * @author Fred Feng
 * @version 1.0
 */
public class MemcachedTemplate implements MemcachedOperations {

	public MemcachedTemplate(MemcachedClient client, MemcachedSerializer serializer) {
		this.client = client;
		this.serializer = serializer;
	}

	private final MemcachedClient client;
	private final MemcachedQueue queue = new MemcachedQueue(this);
	private final MemcachedSerializer serializer;

	public boolean set(String key, int expiration, Object value) throws Exception {
		byte[] bytes = serializer.serialize(value);
		return client.set(key, expiration, bytes);
	}

	public <T> T get(String key, Class<T> requiredType) throws Exception {
		byte[] bytes = client.get(key);
		return serializer.deserialize(bytes, requiredType);
	}

	public boolean delete(String key) throws Exception {
		return client.delete(key);
	}

	public boolean push(String key, int expiration, Object value) throws Exception {
		return queue.push(key, expiration, value);
	}

	public <T> T pop(String key, Class<T> requiredType) throws Exception {
		return queue.pop(key, requiredType);
	}

	public MemcachedClient getClient() {
		return client;
	}

}
