package com.github.paganini2008.springworld.joblink.cron4j;

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
import com.github.paganini2008.springworld.cluster.utils.ApplicationContextUtils;
import com.github.paganini2008.springworld.joblink.BeanNames;
import com.github.paganini2008.springworld.joblink.Job;
import com.github.paganini2008.springworld.joblink.JobDependencyObservable;
import com.github.paganini2008.springworld.joblink.JobExecutor;
import com.github.paganini2008.springworld.joblink.JobFuture;
import com.github.paganini2008.springworld.joblink.JobKey;
import com.github.paganini2008.springworld.joblink.JobTeam;
import com.github.paganini2008.springworld.joblink.Scheduler;
import com.github.paganini2008.springworld.joblink.model.JobPeer;

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
		TaskFuture taskFuture = taskExecutor.schedule(new SimpleTask(job, attachment), startDate.getTime() - System.currentTimeMillis());
		return new JobFutureImpl(taskFuture);
	}

	@Override
	public JobFuture schedule(JobTeam jobTeam, Date startDate) {
		TaskFuture taskFuture = taskExecutor.schedule(ApplicationContextUtils.autowireBean((Task) jobTeam),
				startDate.getTime() - System.currentTimeMillis());
		return new JobFutureImpl(taskFuture);
	}

	@Override
	public JobFuture schedule(Job job, Object attachment, String cronExpression) {
		TaskFuture taskFuture = taskExecutor.schedule(new SimpleTask(job, attachment), CRON.parse(cronExpression));
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
		TaskFuture taskFuture = taskExecutor.scheduleWithFixedDelay(new SimpleTask(job, attachment),
				startDate.getTime() - System.currentTimeMillis(), period);
		return new JobFutureImpl(taskFuture);
	}

	@Override
	public JobFuture scheduleAtFixedRate(Job job, Object attachment, long period, Date startDate) {
		TaskFuture taskFuture = taskExecutor.scheduleAtFixedRate(new SimpleTask(job, attachment),
				startDate.getTime() - System.currentTimeMillis(), period);
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

	@Override
	public JobFuture schedule(JobTeam jobTeam, String cronExpression) {
		TaskFuture taskFuture = taskExecutor.schedule(ApplicationContextUtils.autowireBean((Task) jobTeam), CRON.parse(cronExpression));
		return new JobFutureImpl(taskFuture);
	}

	@Override
	public JobFuture schedule(JobTeam jobTeam, String cronExpression, Date startDate) {
		return schedule(() -> {
			return schedule(jobTeam, cronExpression);
		}, startDate);
	}

	@Override
	public JobFuture scheduleWithFixedDelay(JobTeam jobTeam, long period, Date startDate) {
		TaskFuture taskFuture = taskExecutor.scheduleWithFixedDelay(ApplicationContextUtils.autowireBean((Task) jobTeam),
				startDate.getTime() - System.currentTimeMillis(), period);
		return new JobFutureImpl(taskFuture);
	}

	@Override
	public JobFuture scheduleAtFixedRate(JobTeam jobTeam, long period, Date startDate) {
		TaskFuture taskFuture = taskExecutor.scheduleAtFixedRate(ApplicationContextUtils.autowireBean((Task) jobTeam),
				startDate.getTime() - System.currentTimeMillis(), period);
		return new JobFutureImpl(taskFuture);
	}

	@Override
	public JobFuture scheduleWithDependency(JobTeam jobTeam, JobKey[] dependencies) {
		return jobDependencyObservable.addDependency(jobTeam, dependencies);
	}

	@Override
	public JobFuture scheduleWithDependency(JobTeam jobTeam, JobKey[] dependencies, Date startDate) {
		return schedule(() -> {
			return jobDependencyObservable.addDependency(jobTeam, dependencies);
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

	@Override
	public JobTeam createJobTeam(Job job, JobPeer[] jobPeers) {
		return new Cron4jJobTeam(job, jobPeers);
	}

	private class SimpleTask implements Task {

		private final Job job;
		private final Object attachment;

		SimpleTask(Job job, Object attachment) {
			super();
			this.job = job;
			this.attachment = attachment;
		}

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
