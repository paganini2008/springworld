package com.github.paganini2008.transport.serializer;

import com.github.paganini2008.devtools.io.SerializationUtils;
import com.github.paganini2008.transport.Tuple;

/**
 * 
 * JdkSerializer
 * 
 * @author Fred Feng
 * 
 * 
 * @version 1.0
 */
public class JdkSerializer implements Serializer {

	private final boolean compress;

	public JdkSerializer() {
		this(true);
	}

	public JdkSerializer(boolean compress) {
		this.compress = compress;
	}

	public byte[] serialize(Tuple tuple) {
		return SerializationUtils.serialize(tuple, compress);
	}

	public Tuple deserialize(byte[] bytes) {
		return (Tuple) SerializationUtils.deserialize(bytes, compress);
	}

}
