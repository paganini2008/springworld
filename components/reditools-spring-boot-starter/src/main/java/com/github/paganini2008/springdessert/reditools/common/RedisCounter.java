package com.github.paganini2008.springdessert.reditools.common;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.support.atomic.RedisAtomicLong;

/**
 * 
 * RedisCounter
 *
 * @author Jimmy Hoff
 *
 * @since 1.0
 */
public class RedisCounter {

	private final RedisAtomicLong longValue;

	public RedisCounter(String name, RedisOperations<String, Long> redisOperations) {
		this.longValue = new RedisAtomicLong(name, redisOperations, 0L);
	}

	public RedisCounter(String name, RedisConnectionFactory connectionFactory) {
		this.longValue = new RedisAtomicLong(name, connectionFactory, 0L);
	}

	public void expire(long timeout, TimeUnit timeUnit) {
		longValue.expire(timeout, timeUnit);
	}

	public void expireAt(Date date) {
		longValue.expireAt(date);
	}

	public void keepAlive(TtlKeeper keeper, int timeout, TimeUnit timeUnit) {
		keeper.keepAlive(longValue.getKey(), timeout, 1, timeUnit);
	}

	public void set(long newValue) {
		longValue.set(newValue);
	}

	public long get() {
		return longValue.get();
	}

	public long getAndIncrement() {
		return longValue.getAndIncrement();
	}

	public long getAndDecrement() {
		return longValue.getAndDecrement();
	}

	public long incrementAndGet() {
		return longValue.incrementAndGet();
	}

	public long decrementAndGet() {
		return longValue.decrementAndGet();
	}

	public long getAndAdd(long delta) {
		return longValue.getAndAdd(delta);
	}

	public long getAndSet(long newValue) {
		return longValue.getAndSet(newValue);
	}

	public long addAndGet(long delta) {
		return longValue.addAndGet(delta);
	}

	public String getKey() {
		return longValue.getKey();
	}

	public String toString() {
		return longValue.toString();
	}

	public void destroy() {
		longValue.expire(1, TimeUnit.SECONDS);
	}

}
