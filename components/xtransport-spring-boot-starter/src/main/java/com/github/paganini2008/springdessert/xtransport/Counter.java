package com.github.paganini2008.springdessert.xtransport;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.data.redis.connection.RedisConnectionFactory;

import com.github.paganini2008.devtools.multithreads.Executable;
import com.github.paganini2008.devtools.multithreads.ThreadUtils;
import com.github.paganini2008.springdessert.reditools.common.RedisCounter;

/**
 * 
 * Counter
 *
 * @author Fred Feng
 * @version 1.0
 */
public final class Counter extends RedisCounter implements Executable {

	public Counter(String name, RedisConnectionFactory connectionFactory) {
		super(name, connectionFactory);
	}

	private final AtomicLong counter = new AtomicLong();
	private final AtomicBoolean running = new AtomicBoolean();

	private volatile long increment;
	private volatile long tps;
	private volatile long totalIncrement;
	private volatile long totalTps;

	public void incrementCount() {
		counter.incrementAndGet();
		super.incrementAndGet();
	}

	public void incrementCount(int value) {
		counter.addAndGet(value);
		super.addAndGet(value);
	}

	public long get(boolean total) {
		return total ? super.get() : counter.get();
	}

	public void start() {
		counter.set(0);
		running.set(true);
		ThreadUtils.scheduleWithFixedDelay(this, 1, TimeUnit.SECONDS);
	}

	public void stop() {
		running.set(false);
		super.destroy();
	}

	public long tps(boolean total) {
		return total ? totalTps : tps;
	}

	@Override
	public boolean execute() {
		long value = get(true);
		if (value > 0) {
			long current = value;
			totalTps = current - totalIncrement;
			totalIncrement = current;
		}

		value = get(false);
		if (value > 0) {
			long current = value;
			tps = current - increment;
			increment = current;
		}
		return running.get();
	}

	public String toString() {
		StringBuilder str = new StringBuilder("<Counter>");
		str.append(" count: ").append(get(false) + "/" + get(true));
		str.append(", tps: ").append(tps(false) + "/" + tps(true));
		return str.toString();
	}

}