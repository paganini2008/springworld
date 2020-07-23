package com.github.paganini2008.springworld.scheduler;

import java.util.Date;
import java.util.concurrent.ScheduledFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.scheduling.support.SimpleTriggerContext;

/**
 * 
 * SpringScheduler
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class SpringScheduler implements Scheduler {

	@Autowired
	private TaskScheduler taskScheduler;

	@Autowired
	private JobExecutor jobExecutor;

	@Value("${spring.application.cluster.scheduler.loadbalance.enabled:true}")
	private boolean loadbalanced;

	@Override
	public Future schedule(Job job, Object arg, String cron) {
		ScheduledFuture<?> future = taskScheduler.schedule(wrapJob(job, arg), new CronTrigger(cron));
		return new FutureImpl(job, future);
	}

	@Override
	public Future scheduleWithFixedDelay(Job job, Object arg, long delay, long period) {
		ScheduledFuture<?> future = taskScheduler.scheduleWithFixedDelay(wrapJob(job, arg), new Date(System.currentTimeMillis() + delay),
				period);
		return new FutureImpl(job, future);
	}

	@Override
	public Future scheduleAtFixedRate(Job job, Object arg, long delay, long period) {
		ScheduledFuture<?> future = taskScheduler.scheduleAtFixedRate(wrapJob(job, arg), new Date(System.currentTimeMillis() + delay),
				period);
		return new FutureImpl(job, future);
	}

	@Override
	public void runJob(Job job, Object arg) {
		jobExecutor.execute(job, arg);
	}

	protected Runnable wrapJob(Job job, Object arg) {
		return () -> {
			runJob(job, arg);
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
	private static class FutureImpl implements Future {

		private final Job job;
		private final ScheduledFuture<?> future;

		FutureImpl(Job job, ScheduledFuture<?> future) {
			this.job = job;
			this.future = future;
		}

		@Override
		public void cancel() {
			future.cancel(true);
		}

		@Override
		public boolean isDone() {
			return future.isDone();
		}

		@Override
		public long getNextExectionTime() {
			Trigger trigger;
			if (job instanceof PeriodicJob) {
				trigger = new PeriodicTrigger(((PeriodicJob) job).getPeriod(), ((PeriodicJob) job).getPeriodTimeUnit());
			} else {
				trigger = new CronTrigger(((CronJob) job).getCronExpression());
			}
			try {
				return trigger.nextExecutionTime(new SimpleTriggerContext()).getTime();
			} catch (RuntimeException e) {
				return -1L;
			}
		}

	}

}
