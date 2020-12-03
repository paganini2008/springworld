package com.github.paganini2008.springdessert.tx;

/**
 * 
 * IdGenerator
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
public interface IdGenerator {

	String generateTransactionId();

	String generateXaTransactionId();

}
