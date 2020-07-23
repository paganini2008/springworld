package com.github.paganini2008.springworld.scheduler;

/**
 * 
 * BroadcastVar
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public interface BroadcastVar {

	Object readVar(String name);

	void writeVar(String name, Object value);
	
	boolean hasVar(String name);
	
	int length();

}
