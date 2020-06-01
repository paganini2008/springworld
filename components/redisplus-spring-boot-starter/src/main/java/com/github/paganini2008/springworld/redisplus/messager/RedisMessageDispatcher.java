package com.github.paganini2008.springworld.redisplus.messager;

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
	
}
