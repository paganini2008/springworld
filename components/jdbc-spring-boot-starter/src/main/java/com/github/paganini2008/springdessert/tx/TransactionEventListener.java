package com.github.paganini2008.springdessert.tx;

import java.util.EventListener;

/**
 * 
 * TransactionEventListener
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
public interface TransactionEventListener extends EventListener {

	void fireTransactionEvent(TransactionEvent event);

}
