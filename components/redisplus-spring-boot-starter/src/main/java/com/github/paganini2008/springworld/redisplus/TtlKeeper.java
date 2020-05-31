package com.github.paganini2008.springworld.redisplus;

import java.util.concurrent.TimeUnit;

import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;

import com.github.paganini2008.devtools.multithreads.Clock;
import com.github.paganini2008.devtools.multithreads.Clock.ClockTask;

/**
 * 
 * TtlKeeper
 *
 * @author Fred Feng
 *
 * @since 1.0
 */
public class TtlKeeper {

	private static final int TTL_RESET_THRESHOLD = 10;
	private final Clock clock = new Clock();

	@Autowired
	@Qualifier(BeanNames.REDIS_TEMPLATE)
	private RedisTemplate<String, Object> redisTemplate;

	@PreDestroy
	public void stop() {
		clock.stop();
	}

	public void keep(String key, long timeout, TimeUnit timeUnit) {
		if (timeUnit.compareTo(TimeUnit.SECONDS) < 0) {
			throw new IllegalArgumentException("Don't accept the TimeUnit: " + timeUnit);
		}
		if (redisTemplate.hasKey(key)) {
			redisTemplate.expire(key, timeout, timeUnit);
			clock.scheduleAtFixedRate(new TtlKeepingTask(key, timeout, timeUnit), 1, 1, TimeUnit.SECONDS);
		}
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
			Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
			if (ttl != null) {
				if (ttl < TTL_RESET_THRESHOLD) {
					redisTemplate.expire(key, timeout, timeUnit);
				}
			}
		}

	}

}
