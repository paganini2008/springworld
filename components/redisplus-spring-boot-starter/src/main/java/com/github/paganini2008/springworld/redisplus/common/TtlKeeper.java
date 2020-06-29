package com.github.paganini2008.springworld.redisplus.common;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisOperations;

import com.github.paganini2008.devtools.multithreads.Clock;
import com.github.paganini2008.devtools.multithreads.ClockTask;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * TtlKeeper
 *
 * @author Fred Feng
 *
 * @since 1.0
 */
@Slf4j
public class TtlKeeper {

	private static final int TTL_RESET_THRESHOLD = 10;
	private final Clock clock;
	private final RedisOperations<String, Object> redisOperations;

	public TtlKeeper(RedisOperations<String, Object> redisOperations) {
		this(new Clock(), redisOperations);
	}

	public TtlKeeper(Clock clock, RedisOperations<String, Object> redisOperations) {
		this.clock = clock;
		this.redisOperations = redisOperations;
	}

	public void stop() {
		clock.stop();
	}

	public void keep(String key, long timeout, TimeUnit timeUnit) {
		if (timeUnit.compareTo(TimeUnit.SECONDS) < 0) {
			throw new IllegalArgumentException("Don't accept the TimeUnit: " + timeUnit);
		}
		if (redisOperations.hasKey(key) && redisOperations.getExpire(key) < 0) {
			redisOperations.expire(key, timeout, timeUnit);
			clock.scheduleAtFixedRate(new TtlKeepingTask(key, timeout, timeUnit), 1, 1, TimeUnit.SECONDS);

			if (log.isTraceEnabled()) {
				log.trace("Keeping redis key {}, current count: {}", key, getKeepingCount());
			}
		}
	}

	public int getKeepingCount() {
		return clock.getTaskCount();
	}

	private class TtlKeepingTask extends ClockTask {

		private final String key;
		private final long timeout;
		private final TimeUnit timeUnit;

		TtlKeepingTask(String key, long timeout, TimeUnit timeUnit) {
			this.key = key;
			this.timeout = timeout;
			this.timeUnit = timeUnit;
		}

		@Override
		protected void runTask() {
			Long ttl = redisOperations.getExpire(key, TimeUnit.SECONDS);
			if (ttl != null) {
				if (ttl < TTL_RESET_THRESHOLD) {
					redisOperations.expire(key, timeout, timeUnit);
				}
			}
		}

	}

}
