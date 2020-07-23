package com.github.paganini2008.springworld.redisplus.serializer;

import org.nustaq.serialization.FSTConfiguration;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

/**
 * 
 * FstRedisSerializer
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class FstRedisSerializer<T> implements RedisSerializer<T> {

	private final FSTConfiguration configuration = FSTConfiguration.createDefaultConfiguration();

	private final Class<T> requiredType;

	public FstRedisSerializer(Class<T> requiredType) {
		this.requiredType = requiredType;
	}

	@Override
	public byte[] serialize(T t) throws SerializationException {
		if (t == null) {
			return null;
		}
		return configuration.asByteArray(t);
	}

	@Override
	public T deserialize(byte[] bytes) throws SerializationException {
		if (bytes == null || bytes.length == 0) {
			return null;
		}
		return requiredType.cast(configuration.asObject(bytes));
	}

}
