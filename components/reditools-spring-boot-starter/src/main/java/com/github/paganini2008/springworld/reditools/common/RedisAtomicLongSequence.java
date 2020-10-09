package com.github.paganini2008.springworld.reditools.common;

import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.support.atomic.RedisAtomicLong;

import com.github.paganini2008.devtools.multithreads.AtomicLongSequence;

/**
 * 
 * RedisAtomicLongSequence
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class RedisAtomicLongSequence extends Number {

	private static final long serialVersionUID = 3280933987843776280L;

	private final RedisAtomicLong l;

	private long maxValue;
	private final long initialValue;

	public RedisAtomicLongSequence(String name, RedisConnectionFactory connectionFactory) {
		this(name, connectionFactory, 0);
	}

	public RedisAtomicLongSequence(String name, RedisConnectionFactory connectionFactory, long initialValue) {
		this(name, connectionFactory, initialValue, Long.MAX_VALUE);
	}

	public RedisAtomicLongSequence(String name, RedisConnectionFactory connectionFactory, long initialValue, long maxValue) {
		if (initialValue < 0) {
			throw new IllegalArgumentException("Initial value must >= 0.");
		}
		if (initialValue >= maxValue) {
			throw new IllegalArgumentException("Maximum value must > initial value.");
		}
		l = new RedisAtomicLong(name, connectionFactory, initialValue);
		this.initialValue = initialValue;
		this.maxValue = maxValue;
	}

	public long getAndIncrement() {
		while (true) {
			long current = l.get();
			long next = (current >= maxValue ? initialValue : current + 1);
			if (l.compareAndSet(current, next)) {
				return current;
			}
		}
	}

	public long getAndDecrement() {
		while (true) {
			long current = l.get();
			long next = (current <= initialValue ? maxValue : current - 1);
			if (l.compareAndSet(current, next)) {
				return current;
			}
		}
	}

	public long incrementAndGet() {
		while (true) {
			long current = l.get();
			long next = (current >= maxValue ? initialValue : current + 1);
			if (l.compareAndSet(current, next)) {
				return next;
			}
		}
	}

	public long decrementAndGet() {
		while (true) {
			long current = l.get();
			long next = (current <= initialValue ? maxValue : current - 1);
			if (l.compareAndSet(current, next)) {
				return next;
			}
		}
	}

	public long get() {
		return l.get();
	}

	public void set(long newValue) {
		if (newValue >= initialValue && newValue <= maxValue) {
			l.set(newValue);
		} else {
			throw new IllegalArgumentException("New value must >= " + initialValue + " and <= " + maxValue + ".");
		}
	}

	public long getAndSet(long newValue) {
		if (newValue >= initialValue && newValue <= maxValue) {
			return l.getAndSet(newValue);
		}
		throw new IllegalArgumentException("New value must >= " + initialValue + " and <= " + maxValue + ".");
	}

	public long getAndAdd(long delta) {
		while (true) {
			long current = l.get();
			long next = (current >= maxValue - delta ? initialValue : current + delta);
			if (l.compareAndSet(current, next)) {
				return current;
			}
		}
	}

	public long addAndGet(long delta) {
		while (true) {
			long current = l.get();
			long next = (current >= maxValue - delta ? initialValue : current + delta);
			if (l.compareAndSet(current, next)) {
				return next;
			}
		}
	}

	public void setMaxValue(long maxValue) {
		this.maxValue = maxValue;
	}

	public boolean compareAndSet(long expect, long update) {
		if (update >= initialValue && update <= maxValue) {
			return l.compareAndSet(expect, update);
		}
		throw new IllegalArgumentException("New value must >= " + initialValue + " and <= " + maxValue + ".");
	}

	public byte byteValue() {
		return l.byteValue();
	}

	public short shortValue() {
		return l.shortValue();
	}

	public int intValue() {
		return l.intValue();
	}

	public long longValue() {
		return l.longValue();
	}

	public float floatValue() {
		return l.floatValue();
	}

	public double doubleValue() {
		return l.doubleValue();
	}

	public String toString() {
		return l.toString();
	}

	public int hashCode() {
		int prime = 31;
		prime = prime + Long.hashCode(get());
		return prime;
	}

	public boolean equals(Object other) {
		if (other instanceof AtomicLongSequence) {
			AtomicLongSequence integer = (AtomicLongSequence) other;
			return integer.get() == get();
		}
		return false;
	}

}
