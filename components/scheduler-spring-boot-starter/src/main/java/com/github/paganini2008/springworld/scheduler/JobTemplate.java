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
		final Date startTime = new Date();
		final JobKey jobKey = JobKey.of(job);
		RunningState runningState = RunningState.SKIPPED;
		Throwable reason = null;
		try {
			if (isScheduling(jobKey, job)) {
				beforeRun(jobKey, job, startTime);
				if (job.shouldRun(jobKey)) {
					runningState = doRun(jobKey, job, attachment);
				}
			}
		} catch (JobTerminationException e) {
			reason = e.getCause();
			runningState = RunningState.COMPLETED;
			cancel(jobKey, job, runningState, reason);
		} catch (Throwable e) {
			reason = e;
			runningState = RunningState.FAILED;
			if (e instanceof JobException) {
				throw e;
			}
			throw new JobException("An exception occured during job running.", e);
		} finally {
			afterRun(jobKey, job, startTime, runningState, reason);
		}
	}

	protected RunningState doRun(JobKey jobKey, Job job, Object argument) {
		job.prepare(jobKey);

		Object result = null;
		Throwable reason = null;
		boolean success = false;
		try {
			result = job.execute(jobKey, argument);
			success = true;
		} catch (Throwable e) {
			reason = e;
			success = false;
		} finally {
			if (!success) {
				for (int i = 0; i < job.getRetries(); i++) {
					try {
						result = job.execute(jobKey, argument);
						success = true;
						break;
					} catch (Throwable e) {
						reason = e;
					}
				}
			}

			if (success) {
				job.onSuccess(jobKey, result);
			} else {
				job.onFailure(jobKey, reason);
			}
			if (success) {
				notifyDependencies(jobKey, job, result);
			}
		}
		return RunningState.COMPLETED;
	}

	protected void beforeRun(JobKey jobKey, Job job, Date startTime) {
		if (log.isTraceEnabled()) {
			log.trace("Prepare to run Job: " + jobKey);
		}
	}

	protected void afterRun(JobKey jobKey, Job job, Date startTime, RunningState runningState, Throwable reason) {
		if (log.isTraceEnabled()) {
			log.trace("Job is ending with state: " + runningState);
		}
	}

	protected abstract boolean isScheduling(JobKey jobKey, Job job);

	protected void notifyDependencies(JobKey jobKey, Job job, Object result) {
	}

	protected void cancel(JobKey jobKey, Job job, RunningState runningState, Throwable reason) {
	}

}
