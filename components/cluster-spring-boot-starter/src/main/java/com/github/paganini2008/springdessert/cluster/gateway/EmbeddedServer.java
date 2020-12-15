package com.github.paganini2008.springdessert.cluster.gateway;

/**
 * 
 * EmbeddedServer
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
public interface EmbeddedServer {

	int start();

	void stop();

	boolean isStarted();

}
