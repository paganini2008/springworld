package com.github.paganini2008.transport.serializer;

import org.nustaq.serialization.FSTConfiguration;

import com.github.paganini2008.transport.Tuple;

/**
 * 
 * FstSerializer
 * 
 * @author Fred Feng
 * 
 * 
 * @version 1.0
 */
public class FstSerializer implements Serializer {

	private final FSTConfiguration configuration = FSTConfiguration.createDefaultConfiguration();

	public byte[] serialize(Tuple tuple) {
		return configuration.asByteArray(tuple);
	}

	public Tuple deserialize(byte[] bytes) {
		return (Tuple) configuration.asObject(bytes);
	}

}
