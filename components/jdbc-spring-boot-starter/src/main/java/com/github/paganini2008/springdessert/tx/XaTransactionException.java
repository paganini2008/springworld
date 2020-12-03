package com.github.paganini2008.springdessert.tx;

import com.github.paganini2008.devtools.db4j.TransactionException;

/**
 * 
 * XaTransactionException
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
public class XaTransactionException extends TransactionException {

	private static final long serialVersionUID = -2559839261929424883L;

	public XaTransactionException(String msg) {
		super(msg);
	}

	public XaTransactionException(String msg, Throwable cause) {
		super(msg, cause);
	}

	public XaTransactionException(Throwable cause) {
		super(cause);
	}

}
