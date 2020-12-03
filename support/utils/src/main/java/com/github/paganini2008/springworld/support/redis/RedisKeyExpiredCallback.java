package com.github.paganini2008.springworld.support.redis;

/**
 * 
 * RedisKeyExpiredCallback
 *
 * @author Jimmy Hoff
 * 
 * 
 * @version 1.0
 */
public interface RedisKeyExpiredCallback {

	void handleKeyExpired(String expiredKey, Object expiredValue);

}
