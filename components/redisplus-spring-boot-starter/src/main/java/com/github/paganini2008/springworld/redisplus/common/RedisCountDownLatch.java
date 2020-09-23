package com.github.paganini2008.springworld.redisplus.common;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.github.paganini2008.springworld.redisplus.messager.RedisMessageHandler;
import com.github.paganini2008.springworld.redisplus.messager.RedisMessageSender;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * RedisCountDownLatch
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Slf4j
public class RedisCountDownLatch {

	private static final String DEFAULT_LATCH_NAME_PREFIX = "countdown-latch:";
	private final String channelName;
	private final RedisMessageSender redisMessageSender;

	public RedisCountDownLatch(String name, RedisMessageSender redisMessageSender) {
		this.channelName = DEFAULT_LATCH_NAME_PREFIX + name;
		this.redisMessageSender = redisMessageSender;
	}

	public void countdown(Object attachment) {
		countdown(1, attachment);
	}

	public void countdown(int permits, Object attachment) {
		for (int i = 0; i < permits; i++) {
			redisMessageSender.sendMessage(channelName, attachment);
		}
	}

	public Object[] await(int permits) {
		final String beanName = UUID.randomUUID().toString();
		CountDownLatch latch = new CountDownLatch(permits);
		Referee referee = new Referee(latch);
		redisMessageSender.subscribeChannel(beanName, referee);
		try {
			latch.await();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			log.error(e.getMessage(), e);
		}
		redisMessageSender.unsubscribeChannel(beanName);
		return referee.getMessages();
	}

	public Object[] await(int permits, long timeout, TimeUnit timeUnit) {
		final String beanName = UUID.randomUUID().toString();
		CountDownLatch latch = new CountDownLatch(permits);
		Referee referee = new Referee(latch);
		redisMessageSender.subscribeChannel(beanName, referee);
		try {
			latch.await(timeout, timeUnit);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			log.error(e.getMessage(), e);
		}
		redisMessageSender.unsubscribeChannel(beanName);
		return referee.getMessages();
	}

	/**
	 * 
	 * Referee
	 * 
	 * @author Fred Feng
	 *
	 * @since 1.0
	 */
	private class Referee implements RedisMessageHandler {

		private final CountDownLatch latch;
		private final List<Object> messages = new ArrayList<Object>();

		Referee(CountDownLatch latch) {
			this.latch = latch;
		}

		@Override
		public String getChannel() {
			return channelName;
		}

		@Override
		public synchronized void onMessage(String channel, Object message) throws Exception {
			messages.add(message);
			latch.countDown();
		}

		@Override
		public boolean isRepeatable() {
			return true;
		}

		public Object[] getMessages() {
			return messages.toArray();
		}

	}

}
