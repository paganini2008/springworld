package com.github.paganini2008.springdessert.xtransport.buffer;

import java.util.List;

import com.github.paganini2008.xtransport.Tuple;

/**
 * 
 * BufferZone
 * 
 * @author Jimmy Hoff
 *
 * @since 1.0
 */
public interface BufferZone {
	
	static String DEFAULT_KEY_FORMAT = "spring:application:cluster:%s:transport:bufferzone:%s:%s";

	void set(String collectionName, Tuple tuple) throws Exception;

	List<Tuple> get(String collectionName, int pullSize) throws Exception;

	long size(String collectionName) throws Exception;

}
