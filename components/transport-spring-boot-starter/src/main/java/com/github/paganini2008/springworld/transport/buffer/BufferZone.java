package com.github.paganini2008.springworld.transport.buffer;

import com.github.paganini2008.transport.Tuple;

/**
 * 
 * BufferZone
 * 
 * @author Fred Feng
 * @version 1.0
 */
public interface BufferZone {

	default void configure() throws Exception {
	}

	default void destroy() {
	}

	void set(String collectionName, Tuple tuple) throws Exception;

	Tuple get(String collectionName) throws Exception;

	int size(String collectionName) throws Exception;

}
