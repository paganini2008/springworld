package com.github.paganini2008.springworld.redis.concurrents;

/**
 * 
 * KeepAliveHandler
 *
 * @author Fred Feng
 * 
 * 
 * @version 1.0
 */
public interface KeepAliveHandler {

	void keepAlive(Lifespan lifespan, int checkInterval);
	
}
