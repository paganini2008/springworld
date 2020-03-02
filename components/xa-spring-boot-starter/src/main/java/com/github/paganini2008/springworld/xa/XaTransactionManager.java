package com.github.paganini2008.springworld.xa;

/**
 * 
 * XaTransactionManager
 *
 * @author Fred Feng
 * @version 1.0
 */
public interface XaTransactionManager {

	static final String XA_HTTP_REQUEST_IDENTITY = "xaid";

	XaTransaction openTransaction();

	void closeTransaction();

}