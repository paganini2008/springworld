package com.github.paganini2008.springdessert.tx;

/**
 * 
 * Transaction
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
public interface Transaction {

	boolean commit();

	boolean rollback();

	boolean isCompleted();

	String getId();

	long getStartTime();

}
