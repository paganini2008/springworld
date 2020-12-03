package com.github.paganini2008.embeddedio;

import com.github.paganini2008.devtools.io.SerializationUtils;

/**
 * 
 * ObjectSerialization
 *
 * @author Jimmy Hoff
 * @since 1.0
 */
public class ObjectSerialization implements Serialization {

	public byte[] serialize(Object serializable) {
		return SerializationUtils.serialize(serializable, false);
	}

	public Object deserialize(byte[] bytes) {
		return SerializationUtils.deserialize(bytes, false);
	}

}
