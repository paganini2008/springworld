package com.github.paganini2008.xtransport;

/**
 * 
 * Client
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public interface Client {

	void send(Object data);

	void send(Object data, Partitioner partitioner);

}
