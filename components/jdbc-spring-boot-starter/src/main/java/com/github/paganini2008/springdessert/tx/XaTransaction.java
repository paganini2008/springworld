package com.github.paganini2008.springdessert.tx;

/**
 * 
 * XaTransaction
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
public interface XaTransaction extends Transaction {

	String getXaId();

}
