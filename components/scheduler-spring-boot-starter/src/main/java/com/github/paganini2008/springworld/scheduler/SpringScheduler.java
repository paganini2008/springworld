package com.github.paganini2008.springworld.scheduler;

import java.util.Date;
import java.util.concurrent.ScheduledFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.scheduling.support.SimpleTriggerContext;

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

	@Qualifier("cluster-job-scheduler")
	@Autowired
	private TaskScheduler taskScheduler;

	@Autowired
	private JobExecutor jobExecutor;

	@Autowired
	private JobDependencyObservable jobDependencyObservable;

	@Override
	public JobFuture schedule(Job job, Object arg, String cron) {
		ScheduledFuture<?> future = taskScheduler.schedule(wrapJob(job, arg), new CronTrigger(cron));
		return new FutureImpl(job, future);
	}

	@Override
	public JobFuture scheduleWithFixedDelay(Job job, Object arg, long delay, long period) {
		ScheduledFuture<?> future = taskScheduler.scheduleWithFixedDelay(wrapJob(job, arg), new Date(System.currentTimeMillis() + delay),
				period);
		return new FutureImpl(job, future);
	}

	@Override
	public JobFuture scheduleAtFixedRate(Job job, Object arg, long delay, long period) {
		ScheduledFuture<?> future = taskScheduler.scheduleAtFixedRate(wrapJob(job, arg), new Date(System.currentTimeMillis() + delay),
				period);
		return new FutureImpl(job, future);
	}

	@Override
	public JobFuture scheduleWithDependency(Job job, String[] dependencies) {
		jobDependencyObservable.addDependency(job, dependencies);
		return JobFuture.EMPTY;
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
	private static class FutureImpl implements JobFuture {

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
		public boolean isCancelled() {
			return future.isCancelled();
		}

		@Override
		public long getNextExectionTime(Date lastExecutionTime, Date lastActualExecutionTime) {
			TriggerType triggerType = job.getTrigger().getTriggerType();
			TriggerDescription triggerDescription = job.getTrigger().getTriggerDescription();
			Trigger trigger;
			switch (triggerType) {
			case CRON:
				trigger = new PeriodicTrigger(triggerDescription.getPeriod(), triggerDescription.getPeriodSchedulingUnit().getTimeUnit());
				break;
			case PERIODIC:
				trigger = new CronTrigger(triggerDescription.getCron());
				break;
			default:
				throw new IllegalStateException();
			}
			try {
				return trigger
						.nextExecutionTime(new SimpleTriggerContext(lastExecutionTime, lastActualExecutionTime, lastActualExecutionTime))
						.getTime();
			} catch (RuntimeException e) {
				log.error(e.getMessage(), e);
				return -1L;
			}
		}

	}

	public static void main(String[] args) {
		Trigger trigger = new CronTrigger("*/5 * * * * ?");
		final Date date = DateUtils.setTime(new Date(), 22, 0, 5);
		System.out.println(DateUtils.format(trigger.nextExecutionTime(new SimpleTriggerContext(date, date, date))));
	}

}
