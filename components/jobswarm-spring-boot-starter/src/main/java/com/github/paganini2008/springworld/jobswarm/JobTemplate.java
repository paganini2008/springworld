package com.github.paganini2008.springworld.jobswarm;

import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.paganini2008.devtools.multithreads.ExecutorUtils;
import com.github.paganini2008.springworld.jobswarm.model.JobPeerResult;

/**
 * 
 * JobTemplate
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public abstract class JobTemplate implements JobExecutor {

	protected final Logger log = LoggerFactory.getLogger(JobExecutor.class);

	private Executor threadPool;
	private Logger customizedLog;

	public void setCustomizedLog(Logger logger) {
		if (logger != null) {
			this.customizedLog = logger;
		}
	}

	public void setThreadPool(Executor threadPool) {
		if (threadPool != null) {
			this.threadPool = threadPool;
		}
	}

	protected final void runJob(Job job, Object attachment, int retries) {
		final Date startTime = new Date();
		final JobKey jobKey = JobKey.of(job);
		final long traceId = getTraceId(jobKey);
		RunningState runningState = RunningState.SKIPPED;
		Object result = null;
		Throwable reason = null;
		final Logger log = customizedLog != null ? customizedLog : this.log;
		try {
			if (isScheduling(jobKey, job)) {
				beforeRun(traceId, jobKey, job, attachment, startTime);
				if (job.shouldRun(jobKey, log)) {
					Object[] answer = doRun(jobKey, job, attachment, retries, log);
					runningState = (RunningState) answer[0];
					result = answer[1];
				}
			}
		} catch (JobTerminationException e) {
			reason = e.getCause();
			runningState = RunningState.FINISHED;
			cancel(jobKey, job, runningState, e.getMessage(), reason);
		} catch (Throwable e) {
			reason = e;
			runningState = RunningState.FAILED;
			throw ExceptionUtils.wrapExeception("An exception occured during job running.", e);
		} finally {
			printError(reason, log);
			afterRun(traceId, jobKey, job, attachment, startTime, runningState, result, reason, retries);
		}
	}

	protected abstract long getTraceId(JobKey jobKey);

	protected Object[] doRun(JobKey jobKey, Job job, Object attachment, int retries, Logger log) {
		if (retries > 0) {
			if (log.isTraceEnabled()) {
				log.trace("Retry to run job '{}' on {} times again.", jobKey, retries);
			}
		}

		job.prepare(jobKey, log);

		RunningState runningState = RunningState.COMPLETED;
		Object result = null;
		Throwable reason = null;
		boolean success = false, finished = false;
		try {
			if (threadPool instanceof ExecutorService) {
				Future<Object> future = ((ExecutorService) threadPool).submit(() -> {
					return job.execute(jobKey, attachment, log);
				});
				if (job.getTimeout() > 0) {
					result = future.get(job.getTimeout(), TimeUnit.MILLISECONDS);
				} else {
					result = future.get();
				}
			} else {
				result = job.execute(jobKey, attachment, log);
			}
			success = true;
		} catch (JobTerminationException e) {
			finished = true;
			throw e;
		} catch (ExecutionException e) {
			Throwable real = e.getCause();
			if (real != null) {
				if (real instanceof JobTerminationException) {
					finished = true;
					throw (JobTerminationException) real;
				} else {
					reason = e;
					success = false;
				}
			}
		} catch (Throwable e) {
			reason = e;
			success = false;
		} finally {
			if (!success && !finished) {
				if (retries < job.getRetries()) {
					try {
						result = retry(jobKey, job, attachment, reason, retries + 1, log);
						success = true;
					} catch (JobTerminationException e) {
						throw e;
					} catch (Throwable e) {
						reason = e;
						success = false;
					}
				}
			}

			if (success) {
				job.onSuccess(jobKey, result, log);
			} else {
				printError(reason, log);

				runningState = finished ? RunningState.FINISHED : RunningState.FAILED;
				job.onFailure(jobKey, reason, log);
			}

			notifyDependants(jobKey, job, new JobPeerResult(jobKey, attachment, runningState, result));
		}
		return new Object[] { runningState, result };
	}

	protected void beforeRun(long traceId, JobKey jobKey, Job job, Object attachment, Date startDate) {
		if (log.isTraceEnabled()) {
			if (traceId > 0) {
				log.trace("Prepare to run Job '{}' with traceId '{}'", jobKey, traceId);
			} else if (traceId == 0) {
				log.trace("Load balance on job '{}'", jobKey);
			}
		}
	}

	protected void afterRun(long traceId, JobKey jobKey, Job job, Object attachment, Date startDate, RunningState runningState,
			Object result, Throwable reason, int retries) {
		if (log.isTraceEnabled()) {
			if (traceId > 0) {
				log.trace("Job {} with traceId '{}' is ending with state {}", jobKey, traceId, runningState);
			}
		}
	}

	protected abstract boolean isScheduling(JobKey jobKey, Job job);

	protected Object retry(JobKey jobKey, Job job, Object attachment, Throwable reason, int retries, Logger log) throws Throwable {
		return null;
	}

	protected void notifyDependants(JobKey jobKey, Job job, Object result) {
	}

	protected void cancel(JobKey jobKey, Job job, RunningState runningState, String msg, Throwable reason) {
	}

	protected void printError(Throwable e, Logger log) {
		if (e != null) {
			log.error(e.getMessage(), e);
		}
	}

	public void destroy() {
		if (threadPool != null) {
			ExecutorUtils.gracefulShutdown(threadPool, 60000);
			log.info("Destroy threadPool: " + threadPool);
		}
	}

}
