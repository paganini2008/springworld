package com.github.paganini2008.springworld.tx;

import com.github.paganini2008.devtools.db4j.TransactionException;

/**
 * 
 * TransactionTimeoutException
 *
 * @author Fred Feng
 * @version 1.0
 */
public class TransactionTimeoutException extends TransactionException {
	
	public TransactionTimeoutException() {
		super();
	}

	public TransactionTimeoutException(String msg) {
		super(msg);
	}

	private static final long serialVersionUID = 8290178744628273231L;

}
