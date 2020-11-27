package com.github.paganini2008.springdessert.xmemcached.serializer;

import org.nustaq.serialization.FSTConfiguration;

/**
 * 
 * FstMemcachedSerializer
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class FstMemcachedSerializer implements MemcachedSerializer {

	private final FSTConfiguration configuration = FSTConfiguration.createDefaultConfiguration();

	@Override
	public byte[] serialize(Object object) throws Exception {
		if (object == null) {
			return null;
		}
		return configuration.asByteArray(object);
	}

	@Override
	public <T> T deserialize(byte[] bytes, Class<T> requiredType) throws Exception {
		if (bytes == null || bytes.length == 0) {
			return null;
		}
		return requiredType.cast(configuration.asObject(bytes));
	}

}
