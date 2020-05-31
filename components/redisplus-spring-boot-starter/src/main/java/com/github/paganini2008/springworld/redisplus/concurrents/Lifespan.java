package com.github.paganini2008.springworld.redisplus.concurrents;

import java.util.concurrent.TimeUnit;

/**
 * 
 * Lifespan
 *
 * @author Fred Feng
 * 
 * 
 * @version 1.0
 */
public interface Lifespan {

	void watch(String key, long timeout, long checkInterval, TimeUnit timeUnit);

	void expire(String key);

}
