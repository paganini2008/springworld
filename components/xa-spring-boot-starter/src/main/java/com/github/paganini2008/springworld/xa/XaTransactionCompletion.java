package com.github.paganini2008.springworld.xa;

import com.github.paganini2008.springworld.redis.pubsub.RedisMessageHandler;

/**
 * 
 * XaTransactionCompletion
 *
 * @author Fred Feng
 * @version 1.0
 */
public class XaTransactionCompletion implements RedisMessageHandler {

	@Override
	public String getChannel() {
		return "completion:*";
	}

	@Override
	public void onMessage(String channel, Object message) {
		XaTransactionContext.current().complete((XaTransactionResponse) message);
	}

}
