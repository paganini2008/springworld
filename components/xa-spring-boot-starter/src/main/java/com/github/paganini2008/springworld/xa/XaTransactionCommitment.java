package com.github.paganini2008.springworld.xa;

import com.github.paganini2008.springworld.redis.pubsub.RedisMessageHandler;
import com.github.paganini2008.springworld.redis.pubsub.RedisMessageSender;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * XaTransactionCommitment
 *
 * @author Fred Feng
 * @version 1.0
 */
@Slf4j
public class XaTransactionCommitment implements RedisMessageHandler {

	private final XaTransaction transaction;
	private final XaTransactionManager transactionManager;
	private final RedisMessageSender redisMessageSender;

	public XaTransactionCommitment(XaTransaction transaction, XaTransactionManager transactionManager,
			RedisMessageSender redisMessageSender) {
		this.transaction = transaction;
		this.transactionManager = transactionManager;
		this.redisMessageSender = redisMessageSender;
	}

	@Override
	public String getChannel() {
		return "commitment:" + transaction.getXaId() + ":" + transaction.getId();
	}

	@Override
	public void onMessage(String channel, Object message) {
		log.info(">>> XaTransactionCommitment accept: " + message);
		boolean ok = (Boolean) message;
		XaTransactionResponse response;
		if (ok) {
			response = transaction.commit();
		} else {
			response = transaction.rollback();
		}
		log.info(">>> XaTransactionCommitment response: " + response);
		redisMessageSender.sendMessage("completion:" + transaction.getXaId() + ":" + transaction.getId(), response);
		redisMessageSender.unsubscribeChannel(transaction.getId());

		transactionManager.closeTransaction(transaction.getXaId());
	}

}
