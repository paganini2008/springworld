package com.github.paganini2008.springdessert.reditools.common;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;

import com.github.paganini2008.devtools.date.DateUtils;
import com.github.paganini2008.devtools.multithreads.Clock;
import com.github.paganini2008.devtools.multithreads.ClockTask;
import com.github.paganini2008.springdessert.reditools.BeanNames;
import com.github.paganini2008.springdessert.reditools.messager.RedisMessageSender;

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
public class TtlKeeper implements DisposableBean {

	private static final int TTL_RESET_THRESHOLD = 10;

	private final Clock clock = new Clock();

	@Qualifier(BeanNames.REDIS_TEMPLATE)
	@Autowired
	private RedisTemplate<String, Object> redisTemplate;

	@Autowired
	private RedisMessageSender redisMessageSender;
	
	public void keepAlive(String key, int timeout) {
		keepAlive(key, timeout, 1);
	}

	public void keepAlive(String key, int timeout, int checkInterval) {
		keepAlive(key, timeout, checkInterval, TimeUnit.SECONDS);
	}

	public void keepAlive(String key, int timeout, int checkInterval, TimeUnit timeUnit) {
		if (timeUnit.compareTo(TimeUnit.SECONDS) < 0) {
			throw new IllegalArgumentException("Don't accept the TimeUnit: " + timeUnit);
		}
		if (redisTemplate.hasKey(key)) {
			redisTemplate.expire(key, timeout, timeUnit);
			long checkIntervalInSec = DateUtils.converToSecond(checkInterval, timeUnit);
			clock.scheduleAtFixedRate(new KeyKeepingTask(key, timeout, timeUnit), checkIntervalInSec, checkIntervalInSec, TimeUnit.SECONDS);

			if (log.isTraceEnabled()) {
				log.trace("Keeping redis key {}, current count: {}", key, getKeepingCount());
			}
		}
	}

	public void keepAlive(String key, Object value, int timeout) {
		keepAlive(key, value, timeout, 1);
	}

	public void keepAlive(String key, Object value, int timeout, int checkInterval) {
		keepAlive(key, value, timeout, checkInterval, TimeUnit.SECONDS);
	}

	public void keepAlive(String key, Object value, int timeout, int checkInterval, TimeUnit timeUnit) {
		if (timeUnit.compareTo(TimeUnit.SECONDS) < 0) {
			throw new IllegalArgumentException("Don't accept the TimeUnit: " + timeUnit);
		}
		redisMessageSender.sendEphemeralMessage(key, value, timeout, timeUnit);
		long checkIntervalInSec = DateUtils.converToSecond(checkInterval, timeUnit);
		clock.scheduleAtFixedRate(new KeyValueKeepingTask(key, value, timeout, timeUnit), checkIntervalInSec, checkIntervalInSec,
				TimeUnit.SECONDS);
		if (log.isTraceEnabled()) {
			log.trace("Keeping redis key {}, current count: {}", key, getKeepingCount());
		}
	}

	public int getKeepingCount() {
		return clock.getTaskCount();
	}

	@Override
	public void destroy() throws Exception {
		clock.stop();
	}

	private class KeyKeepingTask extends ClockTask {

		private final String key;
		private final long timeout;
		private final TimeUnit timeUnit;

		KeyKeepingTask(String key, long timeout, TimeUnit timeUnit) {
			this.key = key;
			this.timeout = timeout;
			this.timeUnit = timeUnit;
		}

		@Override
		protected void runTask() {
			try {
				Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
				if (ttl != null && ttl.longValue() < TTL_RESET_THRESHOLD) {
					redisTemplate.expire(key, timeout, timeUnit);
				}
			} catch (Throwable e) {
				log.error(e.getMessage(), e);
			}
		}

	}

	private class KeyValueKeepingTask extends ClockTask {

		private final String key;
		private final Object value;
		private final long timeout;
		private final TimeUnit timeUnit;

		KeyValueKeepingTask(String key, Object value, long timeout, TimeUnit timeUnit) {
			this.key = key;
			this.value = value;
			this.timeout = timeout;
			this.timeUnit = timeUnit;
		}

		@Override
		protected void runTask() {
			try {
				Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
				if (ttl != null && ttl.longValue() < TTL_RESET_THRESHOLD) {
					redisMessageSender.sendEphemeralMessage(key, value, timeout, timeUnit);
				}
			} catch (Throwable e) {
				log.error(e.getMessage(), e);
			}
		}

	}

}
