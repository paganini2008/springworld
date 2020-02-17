package com.github.paganini2008.springworld.redis.pubsub;

/**
 * 
 * RedisMessageHandler
 * 
 * @author Fred Feng
 * 
 * 
 * @version 1.0
 */
public interface RedisMessageHandler {

	String getChannel();

	void onMessage(Object message);

	default boolean isEphemeral() {
		return false;
	}

}
