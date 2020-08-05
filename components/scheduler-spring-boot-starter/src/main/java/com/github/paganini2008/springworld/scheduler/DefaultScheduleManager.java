package com.github.paganini2008.springworld.scheduler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;

import com.github.paganini2008.devtools.Observable;
import com.github.paganini2008.devtools.date.DateUtils;
import com.github.paganini2008.springworld.cluster.utils.ApplicationContextUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * DefaultScheduleManager
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Slf4j
public class DefaultScheduleManager implements ScheduleManager {

	@Autowired
	private Scheduler scheduler;

	private final Map<Job, JobFuture> schedulingCache = new ConcurrentHashMap<Job, JobFuture>();
	private final Observable trigger = Observable.unrepeatable();

	@Override
	public void schedule(final Job job, final Object attachment) {
		trigger.addObserver((trigger, ignored) -> {
			if (hasScheduled(job)) {
				log.warn("Job '{}' has been scheduled.", job.getSignature());
				return;
			}
			if (job instanceof CronJob) {
				schedulingCache.put(job, scheduler.schedule(job, attachment, ((CronJob) job).getCronExpression()));
			} else if (job instanceof PeriodicJob) {
				final PeriodicJob periodicJob = (PeriodicJob) job;
				long delay = DateUtils.convertToMillis(periodicJob.getDelay(), periodicJob.getDelayTimeUnit());
				long period = DateUtils.convertToMillis(periodicJob.getPeriod(), periodicJob.getPeriodTimeUnit());
				switch (periodicJob.getSchedulingMode()) {
				case FIXED_DELAY:
					schedulingCache.put(job, scheduler.scheduleWithFixedDelay(job, attachment, delay, period));
					break;
				case FIXED_RATE:
					schedulingCache.put(job, scheduler.scheduleAtFixedRate(job, attachment, delay, period));
					break;
				}
			} else {
				throw new JobException("Only support for CronJob or PeriodicJob.");
			}

			JobManager jobManager = ApplicationContextUtils.getBean(JobManager.class);
			try {
				jobManager.setJobState(job, JobState.SCHEDULING);
			} catch (Exception e) {
				throw new JobException(e.getMessage(), e);
			}
			log.info("Schedule job '{}' ok. Current scheduling's number is {}", job.getSignature(), countOfScheduling());
		});
	}

	@Override
	public void unscheduleJob(Job job) {
		if (hasScheduled(job)) {
			JobFuture future = schedulingCache.remove(job);
			if (future != null) {
				future.cancel();
			}
			log.info("Unschedule the job: " + job.getSignature());
		}
	}

	@Override
	public boolean hasScheduled(Job job) {
		return schedulingCache.containsKey(job);
	}

	@Override
	public void doSchedule() {
		trigger.notifyObservers();
		log.info("Do all schedules of jobs now.");
	}

	@Override
	public void runJob(Job job, Object attachment) {
		scheduler.runJob(job, attachment);
	}

	@Override
	public int countOfScheduling() {
		return schedulingCache.size();
	}

	@Override
	public JobFuture getFuture(Job job) {
		if (!schedulingCache.containsKey(job)) {
			throw new JobException("Not scheduling job");
		}
		return schedulingCache.get(job);
	}

	@Override
	public void close() {
		for (Map.Entry<Job, JobFuture> entry : schedulingCache.entrySet()) {
			entry.getValue().cancel();
		}
		schedulingCache.clear();
	}

}
