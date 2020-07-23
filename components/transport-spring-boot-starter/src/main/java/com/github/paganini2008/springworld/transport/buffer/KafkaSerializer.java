package com.github.paganini2008.springworld.transport.buffer;

import java.util.Map;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serializer;

import com.github.paganini2008.transport.Tuple;

/**
 * 
 * KafkaSerializer
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public interface KafkaSerializer extends Serializer<Tuple>, Deserializer<Tuple> {

	default void configure(Map<String, ?> configs, boolean isKey) {
	}
	
	default void close() {
	}

}
