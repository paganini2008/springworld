package com.github.paganini2008.springworld.jdbc.tx;

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
