package com.github.paganini2008.springworld.crontab;

import java.util.Date;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.util.ErrorHandler;

import com.github.paganini2008.devtools.Observable;
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

	@Qualifier(BeanNames.CLUSTER_JOB_SCHEDULER)
	@Autowired
	private TaskExecutor taskExecutor;

	@Qualifier(BeanNames.MAIN_JOB_EXECUTOR)
	@Autowired
	private JobExecutor jobExecutor;

	@Qualifier("scheduler-error-handler")
	@Autowired
	private ErrorHandler errorHandler;

	@Autowired
	private JobDependencyObservable jobDependencyObservable;

	@Override
	public JobFuture schedule(Job job, Object attachment, Date startDate) {
		TaskFuture taskFuture = taskExecutor.schedule(wrapJob(job, attachment), startDate.getTime() - System.currentTimeMillis());
		return new JobFutureImpl(taskFuture);
	}

	@Override
	public JobFuture schedule(Job job, Object attachment, String cronExpression) {
		TaskFuture taskFuture = taskExecutor.schedule(wrapJob(job, attachment), CRON.parse(cronExpression));
		return new JobFutureImpl(taskFuture);
	}

	@Override
	public JobFuture schedule(Job job, Object attachment, String cronExpression, Date startDate) {
		return schedule(() -> {
			return schedule(job, attachment, cronExpression);
		}, startDate);
	}

	@Override
	public JobFuture scheduleWithFixedDelay(Job job, Object attachment, long period, Date startDate) {
		TaskFuture taskFuture = taskExecutor.scheduleWithFixedDelay(wrapJob(job, attachment),
				startDate.getTime() - System.currentTimeMillis(), period);
		return new JobFutureImpl(taskFuture);
	}

	@Override
	public JobFuture scheduleAtFixedRate(Job job, Object attachment, long period, Date startDate) {
		TaskFuture taskFuture = taskExecutor.scheduleAtFixedRate(wrapJob(job, attachment), startDate.getTime() - System.currentTimeMillis(),
				period);
		return new JobFutureImpl(taskFuture);
	}

	@Override
	public JobFuture scheduleWithDependency(Job job, JobKey[] dependencies) {
		return jobDependencyObservable.addDependency(job, dependencies);
	}

	@Override
	public JobFuture scheduleWithDependency(Job job, JobKey[] dependencies, Date startDate) {
		return schedule(() -> {
			return jobDependencyObservable.addDependency(job, dependencies);
		}, startDate);
	}

	private JobFuture schedule(Supplier<JobFuture> supplier, Date startDate) {
		final Observable canceller = Observable.unrepeatable();
		final TaskFuture taskFuture = taskExecutor.schedule(new Task() {
			@Override
			public boolean execute() {
				final JobFuture target = supplier.get();
				canceller.addObserver((ob, arg) -> {
					target.cancel();
				});
				return true;
			}
		}, startDate.getTime() - System.currentTimeMillis());
		return new DelayedJobFuture(taskFuture, canceller);
	}

	@Override
	public void runJob(Job job, Object attachment) {
		jobExecutor.execute(job, attachment, 0);
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
	 * JobFutureImpl
	 * 
	 * @author Fred Feng
	 *
	 * @since 1.0
	 */
	private static class JobFutureImpl implements JobFuture {

		private final TaskFuture taskFuture;

		JobFutureImpl(TaskFuture taskFuture) {
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
		public long getNextExectionTime(Date lastExecutionTime, Date lastActualExecutionTime, Date lastCompletionTime) {
			return taskFuture.getDetail().nextExectionTime();
		}

	}

	/**
	 * 
	 * DelayedJobFuture
	 * 
	 * @author Fred Feng
	 *
	 * @since 1.0
	 */
	private static class DelayedJobFuture extends JobFutureImpl {

		private final Observable canceller;

		DelayedJobFuture(TaskFuture taskFuture, Observable canceller) {
			super(taskFuture);
			this.canceller = canceller;
		}

		@Override
		public void cancel() {
			super.cancel();
			canceller.notifyObservers();
		}

	}

}
