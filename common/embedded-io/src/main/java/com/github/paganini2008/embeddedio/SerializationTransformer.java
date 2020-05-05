package com.github.paganini2008.embeddedio;

import java.util.List;

/**
 * 
 * SerializationTransformer
 *
 * @author Fred Feng
 * @since 1.0
 */
public class SerializationTransformer implements Transformer {

	private Serialization encoder = new ObjectSerialization();
	private Serialization decoder = new ObjectSerialization();

	public void setSerialization(Serialization encoder, Serialization decoder) {
		this.encoder = encoder;
		this.decoder = decoder;
	}

	@Override
	public void transferTo(Object value, IoBuffer buffer) {
		byte[] bytes = encoder.serialize(value);
		buffer.append(bytes);
	}

	@Override
	public void transferFrom(IoBuffer buffer, List<Object> output) {
		Object object;
		while (buffer.hasRemaining(4)) {
			byte[] bytes = buffer.getBytes();
			if (bytes != null) {
				object = decoder.deserialize(bytes);
				output.add(object);
			} else {
				break;
			}
		}
	}

}
