package com.github.paganini2008.springworld.support.redis;

/**
 * 
 * RedisMessageHandler
 * @author Jimmy Hoff
 * 
 * @version 1.0
 */
public interface RedisMessageHandler {
	
	void handleMessage(String channel, Object message);
	
}
