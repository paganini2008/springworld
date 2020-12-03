package com.github.paganini2008.springdessert.xtransport.transport;

/**
 * 
 * NioServer
 * 
 * @author Jimmy Hoff
 * 
 * 
 * @version 1.0
 */
public interface NioServer {

	int start() throws Exception;

	void stop();

	boolean isStarted();

}
