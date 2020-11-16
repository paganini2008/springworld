package com.github.paganini2008.springworld.reditools.common;

/**
 * 
 * ConnectionFailureHandler
 * @author Fred Feng
 *
 * @since 1.0
 */
public interface ConnectionFailureHandler {

	void handleException(Throwable e);
	
}
