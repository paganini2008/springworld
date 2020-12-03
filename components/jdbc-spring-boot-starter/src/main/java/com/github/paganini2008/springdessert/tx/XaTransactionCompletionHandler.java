package com.github.paganini2008.springdessert.tx;

import com.github.paganini2008.springdessert.reditools.messager.MessageHandler;
import com.github.paganini2008.springdessert.reditools.messager.OnMessage;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * XaTransactionCompletionHandler
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
@Slf4j
@MessageHandler("completion:*")
public class XaTransactionCompletionHandler {

	@OnMessage
	public void onMessage(String channel, Object message) {
		if (log.isTraceEnabled()) {
			log.trace(message.toString());
		}
	}

}
