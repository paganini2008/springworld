package com.github.paganini2008.embeddedio;

import com.github.paganini2008.devtools.io.SerializationException;

/**
 * 
 * NoopSerialization
 *
 * @author Jimmy Hoff
 * @since 1.0
 */
public class NoopSerialization implements Serialization {

	@Override
	public byte[] serialize(Object object) {
		try {
			return (byte[]) object;
		} catch (ClassCastException e) {
			throw new SerializationException(e);
		}
	}

	@Override
	public Object deserialize(byte[] bytes) {
		return bytes;
	}

}
