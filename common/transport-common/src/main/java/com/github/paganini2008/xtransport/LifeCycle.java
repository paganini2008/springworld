package com.github.paganini2008.xtransport;

/**
 * 
 * LifeCycle
 * 
 * @author Jimmy Hoff
 * @version 1.0
 */
public interface LifeCycle {

	void open();
	
	void close();
	
	boolean isOpened();
	
}
