package com.github.paganini2008.springworld.xmemcached;

/**
 * 
 * MemcachedSerializer
 *
 * @author Fred Feng
 * @version 1.0
 */
public interface MemcachedSerializer {

	byte[] serialize(Object object) throws Exception;

	<T> T deserialize(byte[] bytes, Class<T> requiredType) throws Exception;

}
