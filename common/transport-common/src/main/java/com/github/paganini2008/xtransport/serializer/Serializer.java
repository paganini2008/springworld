package com.github.paganini2008.xtransport.serializer;

import com.github.paganini2008.xtransport.Tuple;

/**
 * 
 * Serializer
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
public interface Serializer {
	
	byte[] serialize(Tuple tuple);

	Tuple deserialize(byte[] bytes);

}
