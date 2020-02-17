package com.github.paganini2008.springworld.webcrawler.utils;

/**
 * 
 * LongIdentifier
 * 
 * @author Fred Feng
 * 
 * 
 * @version 1.0
 */
public interface LongIdentifier {

	default void configure() {
	}

	default void destroy() {
	}

	void setValue(long initialValue);
	
	long currentValue();

	long nextValue();

	long nextValue(int delta);

}
