package com.github.paganini2008.embeddedio;

import java.util.List;

/**
 * 
 * Transformer
 *
 * @author Jimmy Hoff
 * @since 1.0
 */
public interface Transformer {
	
	default void setSerialization(Serialization serialization) {
		setSerialization(serialization, serialization);
	}

	void setSerialization(Serialization encoder, Serialization decoder);

	void transferTo(Object value, IoBuffer buffer);

	void transferFrom(IoBuffer buffer, List<Object> output);

}
