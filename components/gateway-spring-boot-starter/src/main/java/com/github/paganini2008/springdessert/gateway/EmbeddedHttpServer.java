package com.github.paganini2008.springdessert.gateway;

/**
 * 
 * EmbeddedHttpServer
 *
 * @author Jimmy Hoff
 * 
 * @since 1.0
 */
public interface EmbeddedHttpServer {

	int start() ;
	
	void stop();
	
	boolean isStarted();
	
}
