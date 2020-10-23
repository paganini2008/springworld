package com.github.paganini2008.springworld.jobstorm;

/**
 * 
 * LifeCycle
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public interface LifeCycle {

	default void configure() throws Exception {
	}

	default void close() {
	}

}
