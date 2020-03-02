package com.github.paganini2008.springworld.xa;

import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.data.redis.core.StringRedisTemplate;

import com.github.paganini2008.devtools.multithreads.ThreadUtils;
import com.github.paganini2008.springworld.redis.pubsub.RedisMessageHandler;
import com.github.paganini2008.springworld.redis.pubsub.RedisMessageSender;

/**
 * 
 * XaTransactionalBarrier
 *
 * @author Fred Feng
 * @version 1.0
 */
public class XaTransactionalBarrier implements RedisMessageHandler {

	private final String xaId;
	private final RedisMessageSender redisMessageSender;
	private final StringRedisTemplate redisTemplate;
	private final AtomicBoolean canCommit;

	public XaTransactionalBarrier(String xaId, RedisMessageSender redisMessageSender, StringRedisTemplate redisTemplate) {
		this.xaId = xaId;
		this.redisMessageSender = redisMessageSender;
		this.redisTemplate = redisTemplate;
		this.canCommit = new AtomicBoolean(false);
	}

	private String transactionId;
	private long timeout = 60000L;

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	public void join(String transactionId) {
		redisTemplate.opsForList().rightPush(xaId, transactionId);
		redisMessageSender.subscribeChannel(transactionId, this);
		this.transactionId = transactionId;
		ThreadUtils.wait(this, () -> {
			return canCommit.get();
		}, timeout);
	}

	@Override
	public String getChannel() {
		return "commitment:" + xaId;
	}

	@Override
	public void onMessage(String channel, Object message) {
		String id = (String) message;
		if (transactionId.equals(id)) {
			canCommit.set(true);
			ThreadUtils.notify(this);
			redisMessageSender.unsubscribeChannel(id);
		}
	}

}
