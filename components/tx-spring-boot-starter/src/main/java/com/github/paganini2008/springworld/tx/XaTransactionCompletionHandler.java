package com.github.paganini2008.springworld.tx;

import com.github.paganini2008.springworld.redis.pubsub.MessageHandler;
import com.github.paganini2008.springworld.redis.pubsub.OnMessage;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * XaTransactionCompletionHandler
 *
 * @author Fred Feng
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
