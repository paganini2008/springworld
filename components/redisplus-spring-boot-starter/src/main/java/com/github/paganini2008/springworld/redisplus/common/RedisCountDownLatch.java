package com.github.paganini2008.springworld.redisplus.common;

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

	private final String name;
	private final RedisMessageSender redisMessageSender;

	public RedisCountDownLatch(String name, RedisMessageSender redisMessageSender) {
		this.name = name;
		this.redisMessageSender = redisMessageSender;
	}

	private Referee referee = new Referee();

	public void countdown() {
		countdown(1);
	}

	public void countdown(int permits) {
		for (int i = 0; i < permits; i++) {
			redisMessageSender.sendMessage(referee.getChannel(), new byte[0]);
		}
	}

	public void await(int permits) {
		CountDownLatch latch = new CountDownLatch(permits);
		final String beanName = UUID.randomUUID().toString();
		referee.setLatch(latch);
		redisMessageSender.subscribeChannel(beanName, referee);
		try {
			latch.await();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			log.error(e.getMessage(), e);
		}
		redisMessageSender.unsubscribeChannel(beanName);
	}

	public void await(int permits, long timeout, TimeUnit timeUnit) {
		CountDownLatch latch = new CountDownLatch(permits);
		final String beanName = UUID.randomUUID().toString();
		referee.setLatch(latch);
		redisMessageSender.subscribeChannel(beanName, referee);
		try {
			latch.await(timeout, timeUnit);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			log.error(e.getMessage(), e);
		}
		redisMessageSender.unsubscribeChannel(beanName);
	}

	private class Referee implements RedisMessageHandler {

		private CountDownLatch latch;

		public void setLatch(CountDownLatch latch) {
			this.latch = latch;
		}

		@Override
		public void onMessage(String channel, Object message) throws Exception {
			latch.countDown();
		}

		@Override
		public String getChannel() {
			return "countdown:" + name;
		}

		@Override
		public boolean isRepeatable() {
			return true;
		}

	}

}
