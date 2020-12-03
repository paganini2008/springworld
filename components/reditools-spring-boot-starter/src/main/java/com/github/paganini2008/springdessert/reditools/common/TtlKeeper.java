package com.github.paganini2008.springdessert.reditools.common;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.TaskScheduler;

import com.github.paganini2008.devtools.date.DateUtils;
import com.github.paganini2008.springdessert.reditools.BeanNames;
import com.github.paganini2008.springdessert.reditools.messager.RedisMessageSender;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * TtlKeeper
 *
 * @author Jimmy Hoff
 *
 * @since 1.0
 */
@Slf4j
public class TtlKeeper {

	@Qualifier(BeanNames.REDIS_TEMPLATE)
	@Autowired
	private RedisTemplate<String, Object> redisTemplate;

	@Autowired
	private RedisMessageSender redisMessageSender;

	@Autowired
	private TaskScheduler taskScheduler;

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
			taskScheduler.scheduleAtFixedRate(new KeyKeepingTask(key, timeout, timeUnit),
					DateUtils.convertToMillis(checkInterval, timeUnit));
			if (log.isTraceEnabled()) {
				log.trace("Keeping redis key: {}", key);
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
		taskScheduler.scheduleAtFixedRate(new KeyValueKeepingTask(key, value, timeout, timeUnit),
				DateUtils.convertToMillis(checkInterval, timeUnit));
		if (log.isTraceEnabled()) {
			log.trace("Keeping redis key: {}", key);
		}
	}

	private class KeyKeepingTask implements Runnable {

		private final String key;
		private final long timeout;
		private final TimeUnit timeUnit;

		KeyKeepingTask(String key, long timeout, TimeUnit timeUnit) {
			this.key = key;
			this.timeout = timeout;
			this.timeUnit = timeUnit;
		}

		@Override
		public void run() {
			try {
				redisTemplate.expire(key, timeout, timeUnit);
			} catch (Throwable e) {
				log.error(e.getMessage(), e);
			}
		}

	}

	private class KeyValueKeepingTask implements Runnable {

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
		public void run() {
			try {
				redisMessageSender.sendEphemeralMessage(key, value, timeout, timeUnit);
			} catch (Throwable e) {
				log.error(e.getMessage(), e);
			}
		}

	}

}
