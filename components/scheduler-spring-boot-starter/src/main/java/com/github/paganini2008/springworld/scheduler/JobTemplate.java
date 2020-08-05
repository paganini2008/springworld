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
		} catch (Throwable e) {
			if (e instanceof JobCancellationException) {
				runningState = RunningState.COMPLETED;
				throw (JobCancellationException) e;
			}
			runningState = RunningState.FAILED;
			reason = e;
		} finally {
			afterRun(job, now, runningState, reason);
		}
	}

	protected RunningState doRun(Job job, Object arg) throws Exception {
		job.onStart();

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
			job.onEnd();

			if (!continueRun) {
				throw new JobCancellationException(job.getSignature());
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

}
