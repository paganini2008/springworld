package com.github.paganini2008.springworld.scheduler;

/**
 * 
 * JobTemplate
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public abstract class JobTemplate {

	protected void runJob(Job job, Object arg) {
		if (isRunning(job)) {
			beforeRun(job);
			RunningState runningState = doRun(job, arg);
			afterRun(job, runningState);
		} else {
			afterRun(job, RunningState.SKIPPED);
		}
	}

	private RunningState doRun(Job job, Object arg) {
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

			if (success) {
				job.onSuccess(result);
			} else {
				job.onFailure(reason);
			}

			job.onEnd();
		}
		return success ? RunningState.COMPLETED : RunningState.FAILED;
	}

	protected void beforeRun(Job job) {
	}

	protected void afterRun(Job job, RunningState runningState) {
	}

	protected abstract boolean isRunning(Job job);

}
