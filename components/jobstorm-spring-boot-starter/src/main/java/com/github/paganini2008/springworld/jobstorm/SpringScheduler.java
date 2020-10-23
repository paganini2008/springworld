package com.github.paganini2008.springworld.jobstorm;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.scheduling.support.SimpleTriggerContext;

import com.github.paganini2008.devtools.Observable;
import com.github.paganini2008.devtools.date.DateUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * SpringScheduler
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Slf4j
public class SpringScheduler implements Scheduler {

	@Qualifier(BeanNames.CLUSTER_JOB_SCHEDULER)
	@Autowired
	private TaskScheduler taskScheduler;

	@Qualifier(BeanNames.MAIN_JOB_EXECUTOR)
	@Autowired
	private JobExecutor jobExecutor;

	@Autowired
	private SerialDependencyScheduler serialDependencyScheduler;

	private final Map<ScheduledFuture<?>, Trigger> futureTriggers = new ConcurrentHashMap<ScheduledFuture<?>, Trigger>();

	@Override
	public JobFuture schedule(Job job, Object attachment, Date startDate) {
		ScheduledFuture<?> future = taskScheduler.schedule(wrapJob(job, attachment), startDate);
		return new JobFutureImpl(future);
	}

	@Override
	public JobFuture schedule(Job job, Object attachment, String cronExpression) {
		ScheduledFuture<?> future = taskScheduler.schedule(wrapJob(job, attachment), new CronTrigger(cronExpression));
		futureTriggers.put(future, new CronTrigger(cronExpression));
		return new JobFutureImpl(future);
	}

	@Override
	public JobFuture schedule(Job job, Object attachment, String cronExpression, Date startDate) {
		return schedule(() -> {
			return schedule(job, attachment, cronExpression);
		}, startDate);
	}

	@Override
	public JobFuture scheduleWithFixedDelay(Job job, Object attachment, long period, Date startDate) {
		ScheduledFuture<?> future = taskScheduler.scheduleWithFixedDelay(wrapJob(job, attachment), startDate, period);
		futureTriggers.put(future, getPeriodicTrigger(startDate.getTime() - System.currentTimeMillis(), period, false));
		return new JobFutureImpl(future);
	}

	@Override
	public JobFuture scheduleAtFixedRate(Job job, Object attachment, long period, Date startDate) {
		ScheduledFuture<?> future = taskScheduler.scheduleAtFixedRate(wrapJob(job, attachment), startDate, period);
		futureTriggers.put(future, getPeriodicTrigger(startDate.getTime() - System.currentTimeMillis(), period, true));
		return new JobFutureImpl(future);
	}

	private PeriodicTrigger getPeriodicTrigger(long delay, long period, boolean fixedRate) {
		PeriodicTrigger periodicTrigger = new PeriodicTrigger(period, TimeUnit.MILLISECONDS);
		periodicTrigger.setInitialDelay(delay);
		periodicTrigger.setFixedRate(fixedRate);
		return periodicTrigger;
	}

	@Override
	public JobFuture scheduleWithDependency(Job job, JobKey[] dependencies) {
		return serialDependencyScheduler.scheduleDependency(job, dependencies);
	}

	@Override
	public JobFuture scheduleWithDependency(Job job, JobKey[] dependencies, Date startDate) {
		return schedule(() -> {
			return scheduleWithDependency(job, dependencies);
		}, startDate);
	}

	private JobFuture schedule(Supplier<JobFuture> supplier, Date startDate) {
		final Observable canceller = Observable.unrepeatable();
		final ScheduledFuture<?> taskFuture = taskScheduler.schedule(new Runnable() {
			@Override
			public void run() {
				final JobFuture target = supplier.get();
				canceller.addObserver((ob, arg) -> {
					target.cancel();
				});
			}
		}, startDate);
		return new DelayedJobFuture(taskFuture, canceller);
	}

	@Override
	public void runJob(Job job, Object attachment) {
		jobExecutor.execute(job, attachment, 0);
	}

	private Runnable wrapJob(Job job, Object attachment) {
		return () -> {
			runJob(job, attachment);
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
	class JobFutureImpl implements JobFuture {

		private final ScheduledFuture<?> future;

		JobFutureImpl(ScheduledFuture<?> future) {
			this.future = future;
		}

		@Override
		public void cancel() {
			try {
				future.cancel(false);
			} catch (Exception ignored) {
			}
		}

		@Override
		public boolean isDone() {
			return future.isDone();
		}

		@Override
		public boolean isCancelled() {
			return future.isCancelled();
		}

		@Override
		public long getNextExectionTime(Date lastExecutionTime, Date lastActualExecutionTime, Date lastCompletionTime) {
			Trigger trigger = futureTriggers.get(future);
			if (trigger == null) {
				return -1L;
			}
			try {
				return trigger.nextExecutionTime(new SimpleTriggerContext(lastExecutionTime, lastActualExecutionTime, lastCompletionTime))
						.getTime();
			} catch (RuntimeException e) {
				log.error(e.getMessage(), e);
				return -1L;
			}
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
	class DelayedJobFuture extends JobFutureImpl {

		DelayedJobFuture(ScheduledFuture<?> future, Observable canceller) {
			super(future);
			this.canceller = canceller;
		}

		private final Observable canceller;

		@Override
		public void cancel() {
			super.cancel();
			canceller.notifyObservers();
		}
	}

	public static void main(String[] args) {
		Trigger trigger = new CronTrigger("*/5 * * * * ?");
		final Date date = DateUtils.setTime(new Date(), 22, 0, 5);
		System.out.println(DateUtils.format(trigger.nextExecutionTime(new SimpleTriggerContext(date, date, date))));
	}

}
