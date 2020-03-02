package com.github.paganini2008.springworld.xa;

/**
 * 
 * XaTransactionResponse
 *
 * @author Fred Feng
 * @version 1.0
 */
public interface XaTransactionResponse {

	String getXaId();

	String getId();
	
	boolean isCompleted();

	long getElapsedTime();

	String[] getReason();

}