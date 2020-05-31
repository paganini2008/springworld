package com.github.paganini2008.springworld.redisplus.messager;

/**
 * 
 * RedisMessageHandler
 * 
 * @author Fred Feng
 * @version 1.0
 */
public interface RedisMessageHandler {

	String getChannel();

	void onMessage(String channel, Object message) throws Exception;

	default boolean isRepeatable() {
		return true;
	}

	default boolean isAck() {
		return false;
	}

}
