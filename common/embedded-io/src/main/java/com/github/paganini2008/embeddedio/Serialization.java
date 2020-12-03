package com.github.paganini2008.embeddedio;

/**
 * 
 * Serialization
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
public interface Serialization {

	byte[] serialize(Object object);

	Object deserialize(byte[] bytes);

}
