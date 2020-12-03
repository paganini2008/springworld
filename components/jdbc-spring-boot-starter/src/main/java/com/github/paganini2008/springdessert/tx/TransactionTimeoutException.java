package com.github.paganini2008.springdessert.tx;

import com.github.paganini2008.devtools.db4j.TransactionException;

/**
 * 
 * TransactionTimeoutException
 *
 * @author Jimmy Hoff
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
