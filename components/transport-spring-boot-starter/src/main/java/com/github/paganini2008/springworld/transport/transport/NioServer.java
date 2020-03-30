package com.github.paganini2008.springworld.transport.transport;

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
