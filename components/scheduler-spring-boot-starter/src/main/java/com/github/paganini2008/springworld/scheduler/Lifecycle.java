package com.github.paganini2008.springworld.scheduler;

/**
 * 
 * Lifecycle
 *
 * @author Fred Feng
 */
public interface Lifecycle {

	default void configure() throws Exception {
	}
	
	default void close() {
	}
	
}
