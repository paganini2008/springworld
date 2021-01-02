package com.github.paganini2008.springdessert.xtransport.transport;

import java.net.SocketAddress;

/**
 * 
 * NioServer
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
public interface NioServer {

	SocketAddress start() throws Exception;

	void stop();

	boolean isStarted();

}
