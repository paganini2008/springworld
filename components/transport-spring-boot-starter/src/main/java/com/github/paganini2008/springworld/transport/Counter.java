package com.github.paganini2008.springworld.transport;

/**
 * 
 * Counter
 *
 * @author Fred Feng
 * @version 1.0
 */
public interface Counter {

	void reset();

	long incrementAndGet();

	long get();

	void start();

	void stop();

	long tps();

}