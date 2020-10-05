package com.github.paganini2008.springworld.cluster.pool;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.github.paganini2008.devtools.ArrayUtils;
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

	private final AtomicBoolean done = new AtomicBoolean(false);
	private final AtomicBoolean cancelled = new AtomicBoolean(false);
	private final String taskId;
	private final Lock lock = new ReentrantLock();
	private final Condition condition = lock.newCondition();

	public ProcessPoolTaskPromise(String taskId) {
		this.taskId = taskId;
	}

	private volatile Object result;

	@Override
	public Object get() {
		if (isDone()) {
			throw new IllegalStateException("Task is done.");
		}
		while (!isCancelled()) {
			lock.lock();
			try {
				if (result != null) {
					done.set(true);
					return result;
				} else {
					try {
						condition.await(1, TimeUnit.SECONDS);
					} catch (InterruptedException e) {
						break;
					}
				}
			} finally {
				lock.unlock();
			}
		}
		return null;
	}

	@Override
	public Object get(long timeout, TimeUnit timeUnit) {
		if (isDone()) {
			throw new IllegalStateException("Task is done.");
		}
		final long begin = System.nanoTime();
		long elapsed;
		long nanosTimeout = TimeUnit.NANOSECONDS.convert(timeout, timeUnit);
		while (!isCancelled()) {
			lock.lock();
			try {
				if (result != null) {
					done.set(true);
					return result;
				} else {
					if (nanosTimeout > 0) {
						try {
							condition.awaitNanos(nanosTimeout);
						} catch (InterruptedException e) {
							break;
						}
						elapsed = (System.nanoTime() - begin);
						nanosTimeout -= elapsed;
					} else {
						break;
					}
				}
			} finally {
				lock.unlock();
			}
		}
		return null;
	}

	@Override
	public void cancel() {
		cancelled.set(true);
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
		Callback callback = (Callback) message;
		printf(callback.getSignature());
		if (callback instanceof SuccessCallback) {
			result = ((SuccessCallback) callback).getArgument();
		} else {
			Throwable reason = ((FailureCallback) callback).getReason();
			log.error(reason.getMessage(), reason);
		}
		condition.signalAll();
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
