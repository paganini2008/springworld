package com.github.paganini2008.springworld.myjob;

/**
 * 
 * Lifecycle
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public interface Lifecycle {

	default void configure() throws Exception {
	}

	default void close() {
	}

}
