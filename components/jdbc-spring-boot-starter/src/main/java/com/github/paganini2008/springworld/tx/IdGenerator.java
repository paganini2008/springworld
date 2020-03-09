package com.github.paganini2008.springworld.tx;

/**
 * 
 * IdGenerator
 *
 * @author Fred Feng
 * @version 1.0
 */
public interface IdGenerator {

	String generateTransactionId();

	String generateXaTransactionId();

}
