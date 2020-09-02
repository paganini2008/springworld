package com.github.paganini2008.springworld.crontab;

import java.util.Collections;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;

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
public abstract class JobTemplate implements JobExecutor {

	protected final Logger log = LoggerFactory.getLogger(getClass());
	private final Set<JobRuntimeListener> jobRuntimeListeners = Collections.synchronizedNavigableSet(new TreeSet<JobRuntimeListener>());

	@Override
	public void addListener(JobRuntimeListener listener) {
		if (listener != null) {
			jobRuntimeListeners.add(listener);
		}
	}

	@Override
	public void removeListener(JobRuntimeListener listener) {
		if (listener != null) {
			jobRuntimeListeners.remove(listener);
		}
	}

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
			cancel(jobKey, job, runningState, e.getMessage(), reason);
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
				notifyDependants(jobKey, job, result);
			}
		}
		return RunningState.COMPLETED;
	}

	protected void beforeRun(JobKey jobKey, Job job, Date startDate) {
		if (log.isTraceEnabled()) {
			log.trace("Prepare to run Job: " + jobKey);
		}
		for (JobRuntimeListener listener : jobRuntimeListeners) {
			listener.beforeRun(jobKey, startDate);
		}
	}

	protected void afterRun(JobKey jobKey, Job job, Date startDate, RunningState runningState, Throwable reason) {
		if (log.isTraceEnabled()) {
			log.trace("Job is ending with state: " + runningState);
		}
		for (JobRuntimeListener listener : jobRuntimeListeners) {
			listener.afterRun(jobKey, startDate, runningState, reason);
		}
	}

	protected abstract boolean isScheduling(JobKey jobKey, Job job);

	protected void notifyDependants(JobKey jobKey, Job job, Object result) {
	}

	protected void cancel(JobKey jobKey, Job job, RunningState runningState, String msg, Throwable reason) {
	}

}
