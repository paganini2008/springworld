package com.github.paganini2008.springworld.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.util.ErrorHandler;

import com.github.paganini2008.devtools.cron4j.CRON;
import com.github.paganini2008.devtools.cron4j.Task;
import com.github.paganini2008.devtools.cron4j.TaskExecutor;
import com.github.paganini2008.devtools.cron4j.TaskExecutor.TaskFuture;

/**
 * 
 * Cron4jScheduler
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class Cron4jScheduler implements Scheduler {

	@Qualifier("cluster-job-scheduler")
	@Autowired
	private TaskExecutor taskExecutor;

	@Autowired
	private JobExecutor jobExecutor;

	@Qualifier("scheduler-error-handler")
	@Autowired
	private ErrorHandler errorHandler;

	@Override
	public JobFuture schedule(Job job, Object attachment, String cron) {
		TaskFuture taskFuture = taskExecutor.schedule(wrapJob(job, attachment), CRON.parse(cron));
		return new FutureImpl(taskFuture);
	}

	@Override
	public JobFuture scheduleWithFixedDelay(Job job, Object attachment, long delay, long period) {
		TaskFuture taskFuture = taskExecutor.scheduleWithFixedDelay(wrapJob(job, attachment), delay, period);
		return new FutureImpl(taskFuture);
	}

	@Override
	public JobFuture scheduleAtFixedRate(Job job, Object attachment, long delay, long period) {
		TaskFuture taskFuture = taskExecutor.scheduleAtFixedRate(wrapJob(job, attachment), delay, period);
		return new FutureImpl(taskFuture);
	}

	@Override
	public void runJob(Job job, Object attachment) {
		jobExecutor.execute(job, attachment);
	}

	protected Task wrapJob(final Job job, final Object attachment) {
		return new Task() {

			@Override
			public boolean execute() {
				runJob(job, attachment);
				return true;
			}

			@Override
			public boolean onError(Throwable cause) {
				errorHandler.handleError(cause);
				return true;
			}

		};
	}

	/**
	 * 
	 * FutureImpl
	 * 
	 * @author Fred Feng
	 *
	 * @since 1.0
	 */
	private static class FutureImpl implements JobFuture {

		private final TaskFuture taskFuture;

		FutureImpl(TaskFuture taskFuture) {
			this.taskFuture = taskFuture;
		}

		@Override
		public void cancel() {
			taskFuture.cancel();
		}

		@Override
		public boolean isDone() {
			return taskFuture.isDone();
		}

		@Override
		public boolean isCancelled() {
			return taskFuture.isCancelled();
		}

		@Override
		public long getNextExectionTime() {
			return taskFuture.getDetail().nextExectionTime();
		}

	}

}
