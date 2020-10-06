package com.github.paganini2008.springworld.cluster.pool;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import com.github.paganini2008.devtools.ArrayUtils;
import com.github.paganini2008.devtools.date.DateUtils;
import com.github.paganini2008.springworld.reditools.messager.RedisMessageHandler;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * ProcessPoolTaskPromise
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Slf4j
public class ProcessPoolTaskPromise implements TaskPromise, RedisMessageHandler {

	private static final Object lock = new Object();
	private final AtomicBoolean done = new AtomicBoolean(false);
	private final AtomicBoolean cancelled = new AtomicBoolean(false);
	private final String taskId;

	public ProcessPoolTaskPromise(String taskId) {
		this.taskId = taskId;
	}

	private volatile Object returnValue;

	@Override
	public Object get(Supplier<Object> defaultValue) {
		if (isDone()) {
			return getReturnValue(defaultValue);
		}
		while (!isCancelled()) {
			synchronized (lock) {
				if (isDone()) {
					break;
				} else {
					try {
						lock.wait(1000L);
					} catch (InterruptedException e) {
						break;
					}
				}
			}
		}
		return getReturnValue(defaultValue);
	}

	@Override
	public Object get(long timeout, TimeUnit timeUnit, Supplier<Object> defaultValue) {
		if (isDone()) {
			return getReturnValue(defaultValue);
		}
		final long begin = System.nanoTime();
		long elapsed;
		long m = DateUtils.convertToMillis(timeout, timeUnit);
		long n = 0;
		while (!isCancelled()) {
			synchronized (lock) {
				if (isDone()) {
					break;
				} else {
					if (m > 0) {
						try {
							lock.wait(m, (int) n);
						} catch (InterruptedException ignored) {
							break;
						}
						elapsed = (System.nanoTime() - begin);
						m -= elapsed / 1000000L;
						n = elapsed % 1000000L;
					} else {
						break;
					}
				}
			}
		}
		return getReturnValue(defaultValue);
	}

	private Object getReturnValue(Supplier<Object> defaultValue) {
		if (returnValue == null && defaultValue != null) {
			return defaultValue.get();
		}
		return returnValue;
	}

	@Override
	public void cancel() {
		cancelled.set(true);
		synchronized (lock) {
			lock.notifyAll();
		}
	}

	@Override
	public boolean isCancelled() {
		return cancelled.get();
	}

	@Override
	public boolean isDone() {
		return done.get();
	}

	@Override
	public String getChannel() {
		return taskId;
	}

	@Override
	public void onMessage(String channel, Object message) throws Exception {
		if (message instanceof Return) {
			Return callback = (Return) message;
			returnValue = callback.getReturnValue();
			printf(callback.getSignature());
			if (callback instanceof FailureCallback) {
				Throwable reason = ((FailureCallback) callback).getReason();
				log.error(reason.getMessage(), reason);
			}
		}
		done.set(true);
		synchronized (lock) {
			lock.notifyAll();
		}
	}

	@Override
	public boolean isRepeatable() {
		return false;
	}

	private void printf(Signature signature) {
		if (log.isTraceEnabled()) {
			log.trace("[Calling: {}] Executed beanName: {}, beanClassName: {}, methodName: {}, Elapsed: {}", signature.getId(),
					signature.getBeanName(), signature.getBeanClassName(), signature.getMethodName(),
					System.currentTimeMillis() - signature.getTimestamp());
			log.trace("[Calling: {}] Input parameters: {}", signature.getId(), ArrayUtils.toString(signature.getArguments()));
		}
	}

}
