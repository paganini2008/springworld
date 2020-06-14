package com.github.paganini2008.springworld.cluster.scheduler;

import java.util.concurrent.atomic.AtomicBoolean;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * JobBeanProxy
 *
 * @author Fred Feng
 *
 * @since 1.0
 */
@Slf4j
public class DefaultJobBeanProxy implements Runnable, JobBeanProxy {

	private final Job job;
	private final AtomicBoolean running = new AtomicBoolean(true);

	DefaultJobBeanProxy(Job job) {
		this.job = job;
	}

	@Override
	public boolean isRunning() {
		return running.get();
	}

	@Override
	public void pause() {
		running.set(false);
		if (log.isTraceEnabled()) {
			log.trace("Pause job: " + job.getName() + "/" + job.getClass().getName());
		}
	}

	@Override
	public void resume() {
		running.set(true);
		if (log.isTraceEnabled()) {
			log.trace("Resume job: " + job.getName() + "/" + job.getClass().getName());
		}
	}

	@Override
	public void run() {
		if (!isRunning()) {
			return;
		}
		job.onStart();
		Object result = null;
		Throwable reason = null;
		boolean success = false;
		try {
			result = job.execute();
			success = true;
		} catch (Throwable e) {
			reason = e;
			success = false;
		} finally {
			if (!success) {
				for (int i = 0; i < job.retries(); i++) {
					try {
						result = job.execute();
						success = true;
						break;
					} catch (Throwable e) {
						reason = e;
					}
				}
			}
			if (success) {
				job.onSuccess(result);
			} else {
				job.onFailure(reason);
			}
			job.onEnd();
		}
	}

}
