package com.github.paganini2008.springdessert.transport.utils;

import org.nustaq.serialization.FSTConfiguration;

import com.github.paganini2008.transport.Tuple;

/**
 * 
 * FstKafkaSerializer
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class FstKafkaSerializer implements KafkaSerializer {

	private final FSTConfiguration configuration = FSTConfiguration.createDefaultConfiguration();

	@Override
	public byte[] serialize(String topic, Tuple data) {
		return configuration.asByteArray(data);
	}

	@Override
	public Tuple deserialize(String topic, byte[] data) {
		return (Tuple) configuration.asObject(data);
	}

}
