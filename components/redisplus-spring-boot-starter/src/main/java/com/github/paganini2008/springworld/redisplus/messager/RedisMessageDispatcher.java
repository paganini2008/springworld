package com.github.paganini2008.springworld.redisplus.messager;

import java.util.concurrent.TimeUnit;

/**
 * 
 * RedisMessageDispatcher
 *
 * @author Fred Feng
 *
 * @since 1.0
 */
public interface RedisMessageDispatcher {

	void dispatch(RedisMessageEntity messageEntity);

	void expire(String expiredKey, RedisMessageEntity messageEntity, long delay, TimeUnit timeUnit);

}
