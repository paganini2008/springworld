package com.github.paganini2008.springworld.transport;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.data.redis.support.atomic.RedisAtomicLong;

import com.github.paganini2008.devtools.multithreads.Executable;
import com.github.paganini2008.devtools.multithreads.ThreadUtils;

/**
 * 
 * Counter
 *
 * @author Fred Feng
 * @version 1.0
 */
public final class Counter implements Executable {

	public Counter(RedisAtomicLong counter) {
		this.global = counter;
	}

	private final RedisAtomicLong global;
	private final AtomicLong local = new AtomicLong();
	private final AtomicBoolean running = new AtomicBoolean();
	private long localIncrement;
	private long localTps;
	private long globalIncrement;
	private long globalTps;

	public void reset() {
		local.set(0);
		global.set(0);
	}

	public void increment() {
		local.incrementAndGet();
		global.incrementAndGet();
	}

	public long local() {
		return local.get();
	}
	
	public long global() {
		return global.get();
	}

	public void start() {
		running.set(true);
		ThreadUtils.scheduleAtFixedRate(this, 1, TimeUnit.SECONDS);
	}

	public void stop() {
		running.set(false);
	}

	public long localTps() {
		return localTps;
	}

	public long globalTps() {
		return globalTps;
	}

	@Override
	public boolean execute() {
		if (global() > 0) {
			long current = global();
			globalTps = current - globalIncrement;
			globalIncrement = current;
		}
		if (local() > 0) {
			long current = local();
			localTps = current - localIncrement;
			localIncrement = current;
		}
		return running.get();
	}

}
