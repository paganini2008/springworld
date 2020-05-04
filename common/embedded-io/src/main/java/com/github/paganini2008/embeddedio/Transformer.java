package com.github.paganini2008.embeddedio;

import java.util.List;

/**
 * 
 * Transformer
 *
 * @author Fred Feng
 * @since 1.0
 */
public interface Transformer {
	
	default void setSerialization(Serialization serialization) {
		setSerialization(serialization, serialization);
	}

	void setSerialization(Serialization encoder, Serialization decoder);

	void transferTo(Object value, AppendableByteBuffer byteBuffer);

	void transferFrom(AppendableByteBuffer byteBuffer, List<Object> output);

}
