package com.github.paganini2008.springworld.support.redis;

/**
 * 
 * RedisKeyExpiredEventHandler
 *
 * @author Jimmy Hoff
 * 
 * 
 * @version 1.0
 */
public interface RedisKeyExpiredEventHandler {

	void registerCallback(String key, RedisKeyExpiredCallback keyExpiredCallback);

	void registerCallback(String key, int index, RedisKeyExpiredCallback keyExpiredCallback);
}
