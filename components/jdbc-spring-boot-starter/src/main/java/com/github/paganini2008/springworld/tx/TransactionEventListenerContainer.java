package com.github.paganini2008.springworld.tx;

import org.springframework.context.ApplicationListener;

import com.github.paganini2008.devtools.collection.MultiMappedMap;

/**
 * 
 * TransactionEventListenerContainer
 *
 * @author Fred Feng
 * @version 1.0
 */
public class TransactionEventListenerContainer implements ApplicationListener<TransactionEvent> {

	private final MultiMappedMap<TransactionPhase, String, TransactionEventListener> holder = new MultiMappedMap<>();

	public void registerEventListener(TransactionPhase transactionPhase, String id, TransactionEventListener eventListener) {
		holder.put(transactionPhase, id, eventListener);
	}

	@Override
	public void onApplicationEvent(TransactionEvent event) {
		TransactionEventListener eventListener = holder.removeValue(event.getTransactionPhase(), event.getId());
		if (eventListener != null) {
			eventListener.fireTransactionEvent(event);
		}
	}

}
