package com.github.paganini2008.springworld.transport;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.data.redis.support.atomic.RedisAtomicLong;

import com.github.paganini2008.devtools.multithreads.Executable;
import com.github.paganini2008.devtools.multithreads.ThreadUtils;

/**
 * 
 * GlobalCounter
 *
 * @author Fred Feng
 * @version 1.0
 */
public class GlobalCounter implements Executable, Counter {

	public GlobalCounter(RedisAtomicLong counter) {
		this.counter = counter;
	}

	private final RedisAtomicLong counter;
	private final AtomicBoolean running = new AtomicBoolean();
	private long increment;
	private long tps;

	public void reset() {
		counter.set(0);
	}

	public long incrementAndGet() {
		return counter.incrementAndGet();
	}

	public long get() {
		return counter.get();
	}

	public void start() {
		running.set(true);
		ThreadUtils.scheduleAtFixedRate(this, 1, TimeUnit.SECONDS);
	}

	public void stop() {
		running.set(false);
	}

	public long tps() {
		return tps;
	}

	@Override
	public boolean execute() {
		if (get() > 0) {
			long current = get();
			tps = current - increment;
			increment = current;
		}
		return running.get();
	}

}
