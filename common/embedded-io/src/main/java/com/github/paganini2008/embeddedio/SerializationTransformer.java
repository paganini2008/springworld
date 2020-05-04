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
	public void transferTo(Object value, AppendableByteBuffer byteBuffer) {
		byte[] bytes = encoder.serialize(value);
		byteBuffer.append(bytes);
	}

	@Override
	public void transferFrom(AppendableByteBuffer byteBuffer, List<Object> output) {
		Object object;
		while (byteBuffer.hasRemaining(4)) {
			byte[] bytes = byteBuffer.getBytes();
			if (bytes != null) {
				object = decoder.deserialize(bytes);
				output.add(object);
			} else {
				break;
			}
		}
	}

}
