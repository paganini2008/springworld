package com.github.paganini2008.springworld.xmemcached;

import java.io.IOException;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.paganini2008.devtools.io.SerializationException;

/**
 * 
 * JacksonMemcachedSerializer
 *
 * @author Fred Feng
 * @version 1.0
 */
public class JacksonMemcachedSerializer implements MemcachedSerializer {

	private final ObjectMapper objectMapper;

	{
		objectMapper = new ObjectMapper();
		objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	@Override
	public byte[] serialize(Object object) {
		try {
			return objectMapper.writeValueAsBytes(object);
		} catch (IOException e) {
			throw new SerializationException(e.getMessage(), e);
		}
	}

	@Override
	public <T> T deserialize(byte[] bytes, Class<T> requiredType) throws Exception {
		if (bytes == null || bytes.length == 0) {
			return null;
		}
		try {
			return this.objectMapper.readValue(bytes, 0, bytes.length, requiredType);
		} catch (IOException e) {
			throw new SerializationException(e.getMessage(), e);
		}
	}

}
