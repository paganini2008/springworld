package com.github.paganini2008.springworld.scheduler;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * JobTemplate
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public abstract class JobTemplate {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	protected final void runJob(Job job, Object attachment) {
		final Date now = new Date();
		RunningState runningState = RunningState.SKIPPED;
		Throwable reason = null;
		try {
			beforeRun(job, now);
			if (isScheduling(job) && job.shouldRun()) {
				runningState = doRun(job, attachment);
			}
		} catch (JobTerminationException e) {
			reason = e.getCause();
			runningState = RunningState.COMPLETED;
			cancel(job, runningState, reason);
		} catch (Throwable e) {
			reason = e;
			runningState = RunningState.FAILED;
			throw new JobException("An exception occured during job running.", e);
		} finally {
			afterRun(job, now, runningState, reason);
		}
	}

	protected RunningState doRun(Job job, Object arg) {
		job.prepare();

		Object result = null;
		Throwable reason = null;
		boolean success = false;
		try {
			result = job.execute(arg);
			success = true;
		} catch (Throwable e) {
			reason = e;
			success = false;
		} finally {
			if (!success) {
				for (int i = 0; i < job.getRetries(); i++) {
					try {
						result = job.execute(arg);
						success = true;
						break;
					} catch (Throwable e) {
						reason = e;
					}
				}
			}

			boolean continueRun;
			if (success) {
				continueRun = job.onSuccess(result);
			} else {
				continueRun = job.onFailure(reason);
			}

			if (!continueRun) {
				throw reason != null ? new JobTerminationException(job, reason) : new JobTerminationException(job);
			}
			if (success) {
				notifyDependencies(job, result);
			}
		}
		return RunningState.COMPLETED;
	}

	protected void beforeRun(Job job, Date startTime) {
		if (log.isTraceEnabled()) {
			log.trace("Prepare to run Job: " + job.getSignature());
		}
	}

	protected void afterRun(Job job, Date startTime, RunningState runningState, Throwable reason) {
		if (log.isTraceEnabled()) {
			log.trace("Job is end with state: " + runningState);
		}
	}

	protected abstract boolean isScheduling(Job job);

	protected void notifyDependencies(Job job, Object result) {
	}

	protected void cancel(Job job, RunningState runningState, Throwable reason) {
	}

}
