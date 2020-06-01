package com.github.paganini2008.springworld.redisplus.common;

import com.github.paganini2008.devtools.multithreads.latch.Latch;

/**
 * 
 * SharedLatch
 *
 * @author Fred Feng
 * @version 1.0
 */
public interface SharedLatch extends Latch {

	String getKey();

}
