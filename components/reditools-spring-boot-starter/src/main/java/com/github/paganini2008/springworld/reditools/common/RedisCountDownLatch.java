package com.github.paganini2008.springworld.reditools.common;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import com.github.paganini2008.devtools.date.DateUtils;
import com.github.paganini2008.devtools.multithreads.ThreadUtils;
import com.github.paganini2008.springworld.reditools.messager.RedisMessageHandler;
import com.github.paganini2008.springworld.reditools.messager.RedisMessageSender;

/**
 * 
 * RedisCountDownLatch
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class RedisCountDownLatch implements DistributedCountDownLatch {

	private static final String DEFAULT_PREFIX_LATCH_NAME = "countdown-latch:";
	private final String latchName;
	private final RedisMessageSender redisMessageSender;
	private Latch latch;
	private final AtomicBoolean locked = new AtomicBoolean();

	public RedisCountDownLatch(String name, RedisMessageSender redisMessageSender) {
		this.latchName = DEFAULT_PREFIX_LATCH_NAME + name;
		this.redisMessageSender = redisMessageSender;
	}

	public void countdown(Object attachment) {
		countdown(1, attachment);
	}

	public void countdown(int permits, Object attachment) {
		for (int i = 0; i < permits; i++) {
			redisMessageSender.sendMessage(latchName, attachment);
		}
	}

	public Object[] await(int permits, InterruptibleHandler handler) {
		if (locked.get()) {
			throw new IllegalStateException("Operation is being locked.");
		}
		locked.set(true);
		final String beanName = UUID.randomUUID().toString();
		latch = new Latch(permits);
		Referee referee = new Referee(latchName, latch);
		redisMessageSender.subscribeChannel(beanName, referee);
		try {
			latch.doWait();
		} catch (InterruptedException e) {
			if (handler != null) {
				handler.onCancellation();
			}
		} finally {
			locked.set(false);
		}
		redisMessageSender.unsubscribeChannel(beanName);
		return referee.getMessages();
	}

	public Object[] await(int permits, long timeout, TimeUnit timeUnit, InterruptibleHandler handler) {
		if (locked.get()) {
			throw new IllegalStateException("Operation is being locked.");
		}
		locked.set(true);
		final String beanName = UUID.randomUUID().toString();
		latch = new Latch(permits);
		Referee referee = new Referee(latchName, latch);
		redisMessageSender.subscribeChannel(beanName, referee);
		try {
			latch.doWait(timeout, timeUnit);
		} catch (InterruptedException e) {
			if (handler != null) {
				handler.onCancellation();
			}
		} catch (TimeoutException e) {
			if (handler != null) {
				handler.onTimeout();
			}
		} finally {
			locked.set(false);
		}
		redisMessageSender.unsubscribeChannel(beanName);
		return referee.getMessages();
	}

	public void cancel() {
		if (latch != null) {
			latch.doCancel();
		}
	}

	static class Latch {

		Thread currentThread;
		CountDownLatch latch;

		Latch(int permits) {
			this.latch = new CountDownLatch(permits);
			this.currentThread = Thread.currentThread();
		}

		public void doWait() throws InterruptedException {
			latch.await();
		}

		public void doWait(long timeout, TimeUnit timeUnit) throws InterruptedException, TimeoutException {
			long now = System.currentTimeMillis();
			try {
				latch.await(timeout, timeUnit);
			} catch (InterruptedException e) {
				currentThread.interrupt();
				throw e;
			}
			long elapsed = System.currentTimeMillis() - now;
			ThreadUtils.randomSleep(100, 500);
			long timeoutInMs;
			if (elapsed > (timeoutInMs = DateUtils.convertToMillis(timeout, timeUnit))) {
				throw new TimeoutException("Timeout: " + timeoutInMs);
			}
		}

		public void doCountDown() {
			latch.countDown();
		}

		public void doCancel() {
			currentThread.interrupt();
		}

	}

	/**
	 * 
	 * Referee
	 * 
	 * @author Fred Feng
	 *
	 * @since 1.0
	 */
	static class Referee implements RedisMessageHandler {

		private final Latch latch;
		private final String latchName;
		private final List<Object> messages = new CopyOnWriteArrayList<Object>();

		Referee(String latchName, Latch latch) {
			this.latchName = latchName;
			this.latch = latch;
		}

		@Override
		public String getChannel() {
			return latchName;
		}

		@Override
		public void onMessage(String channel, Object message) throws Exception {
			messages.add(message);
			latch.doCountDown();
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
