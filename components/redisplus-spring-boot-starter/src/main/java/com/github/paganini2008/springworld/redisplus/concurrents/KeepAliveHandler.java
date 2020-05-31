package com.github.paganini2008.springworld.redisplus.concurrents;

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
