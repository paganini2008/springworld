package com.github.paganini2008.transport;

/**
 * 
 * LifeCycle
 * 
 * @author Fred Feng
 * 
 * 
 * @version 1.0
 */
public interface LifeCycle {

	void open();
	
	void close();
	
	boolean isOpened();
	
}
