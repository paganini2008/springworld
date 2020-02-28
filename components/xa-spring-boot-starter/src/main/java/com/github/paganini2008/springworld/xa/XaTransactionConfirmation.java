package com.github.paganini2008.springworld.xa;

import com.github.paganini2008.springworld.redis.pubsub.RedisMessageHandler;
import com.github.paganini2008.springworld.redis.pubsub.RedisMessageSender;

/**
 * 
 * XaTransactionConfirmation
 *
 * @author Fred Feng
 * @version 1.0
 */
public class XaTransactionConfirmation implements RedisMessageHandler {

	private final XaTransaction transaction;
	private final RedisMessageSender redisMessageSender;

	public XaTransactionConfirmation(XaTransaction transaction, XaTransactionManager transactionManager,
			RedisMessageSender redisMessageSender) {
		this.transaction = transaction;
		this.redisMessageSender = redisMessageSender;
	}

	@Override
	public String getChannel() {
		return "confirmation:" + transaction.getId();
	}

	@Override
	public void onMessage(String channel, Object message) {
		XaTransactionResponse response;
		if (message instanceof Boolean && (Boolean) message) {
			response = transaction.commit();
		} else {
			response = transaction.rollback();
		}
		redisMessageSender.sendMessage("completion:" + response.getId(), response);
		redisMessageSender.unsubscribeChannel(response.getId());
	}

}
