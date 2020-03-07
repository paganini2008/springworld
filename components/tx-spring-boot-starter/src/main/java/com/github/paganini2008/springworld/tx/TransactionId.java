package com.github.paganini2008.springworld.tx;

/**
 * 
 * TransactionId
 *
 * @author Fred Feng
 * @version 1.0
 */
public interface TransactionId {

	String generateId();

	String generateXaId();

}
