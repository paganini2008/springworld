package com.github.paganini2008.springdessert.tx;

/**
 * 
 * XaTransaction
 *
 * @author Fred Feng
 * @version 1.0
 */
public interface XaTransaction extends Transaction {

	String getXaId();

}
