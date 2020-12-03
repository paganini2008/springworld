package com.github.paganini2008.springdessert.reditools.common;

import com.github.paganini2008.devtools.multithreads.latch.Latch;

/**
 * 
 * SharedLatch
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
public interface SharedLatch extends Latch {

	String getKey();

}
