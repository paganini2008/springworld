package com.github.paganini2008.springworld.support;

/**
 * 
 * IdGenerator
 *
 * @author Jimmy Hoff
 * 
 * 
 * @version 1.0
 */
public interface IdGenerator {

	long currentValue();

	long nextValue();

	default void destroy() {
	}

}
