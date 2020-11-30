package com.github.paganini2008.springdessert.xtransport.transport;

/**
 * 
 * NioServer
 * 
 * @author Fred Feng
 * 
 * 
 * @version 1.0
 */
public interface NioServer {

	int start() throws Exception;

	void stop();

	boolean isStarted();

}
