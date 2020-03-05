package com.github.paganini2008.springworld.xa;

/**
 * 
 * WrappedXaTransactionFactory
 *
 * @author Fred Feng
 * @version 1.0
 */
public class WrappedXaTransactionFactory implements XaTransactionFactory {

	@Override
	public XaTransaction createTransaction(String xaId) {
		return new WrappedXaTransaction(xaId);
	}

}
