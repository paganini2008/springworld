package com.github.paganini2008.springworld.tx;

import java.util.EventListener;

/**
 * 
 * TransactionEventListener
 *
 * @author Fred Feng
 * @version 1.0
 */
public interface TransactionEventListener extends EventListener {

	void fireTransactionEvent(TransactionEvent event);

}
