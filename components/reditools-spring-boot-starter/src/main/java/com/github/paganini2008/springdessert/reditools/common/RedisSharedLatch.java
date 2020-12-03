package com.github.paganini2008.springdessert.reditools.common;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.github.paganini2008.devtools.multithreads.ThreadUtils;

/**
 * 
 * RedisSharedLatch
 *
 * @author Jimmy Hoff
 *
 * @since 1.0
 */
public class RedisSharedLatch implements SharedLatch {

	public RedisSharedLatch(RedisCounter redisCounter, int maxPermits) {
		this.redisCounter = redisCounter;
		this.maxPermits = maxPermits;
		this.startTime = System.currentTimeMillis();
	}

	private final int maxPermits;
	private final long startTime;
	private final Lock lock = new ReentrantLock();
	private final Condition condition = lock.newCondition();
	private final RedisCounter redisCounter;

	public boolean acquire() {
		while (true) {
			lock.lock();
			try {
				if (redisCounter.get() < maxPermits) {
					redisCounter.incrementAndGet();
					return true;
				} else {
					try {
						condition.await();
					} catch (InterruptedException e) {
						break;
					}
				}
			} finally {
				lock.unlock();
			}
		}
		return false;
	}

	public boolean acquire(long timeout, TimeUnit timeUnit) {
		final long begin = System.nanoTime();
		long elapsed;
		long nanosTimeout = TimeUnit.NANOSECONDS.convert(timeout, timeUnit);
		while (true) {
			lock.lock();
			try {
				if (redisCounter.get() < maxPermits) {
					redisCounter.incrementAndGet();
					return true;
				} else {
					if (nanosTimeout > 0) {
						try {
							condition.awaitNanos(nanosTimeout);
						} catch (InterruptedException e) {
							break;
						}
						elapsed = (System.nanoTime() - begin);
						nanosTimeout -= elapsed;
					} else {
						break;
					}
				}
			} finally {
				lock.unlock();
			}
		}
		return false;
	}

	public boolean tryAcquire() {
		if (redisCounter.get() < maxPermits) {
			redisCounter.incrementAndGet();
			return true;
		}
		return false;
	}

	public long cons() {
		return maxPermits - availablePermits();
	}

	public void release() {
		if (!isLocked()) {
			return;
		}
		lock.lock();
		condition.signalAll();
		redisCounter.decrementAndGet();
		lock.unlock();
	}

	public boolean isLocked() {
		return redisCounter.get() > 0;
	}

	public long join() {
		while (isLocked()) {
			ThreadUtils.randomSleep(1000L);
		}
		return System.currentTimeMillis() - startTime;
	}

	public String getKey() {
		return redisCounter.getKey();
	}

	@Override
	public long availablePermits() {
		return maxPermits - redisCounter.get();
	}
}
