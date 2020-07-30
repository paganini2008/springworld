package com.github.paganini2008.springworld.scheduler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;

import com.github.paganini2008.devtools.Observable;
import com.github.paganini2008.devtools.date.DateUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * AbstractJobManager
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Slf4j
public abstract class AbstractJobManager implements JobManager {

	@Autowired
	private Scheduler scheduler;

	@Autowired
	private JobDependency jobDependency;

	private final Map<Job, Future> schedulingCache = new ConcurrentHashMap<Job, Future>();
	private final Observable trigger = Observable.unrepeatable();

	@Override
	public void schedule(final Job job, final Object attachment)throws JobException{
		trigger.addObserver((trigger, ignored) -> {
			if (!hasScheduled(job)) {
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
				} else if (job instanceof SerializableJob) {
					jobDependency.addDependency((SerializableJob) job);
				} else {
					throw new JobException("Please implement the job interface for CronJob or ScheduledJob.");
				}

				setJobState(job, JobState.SCHEDULING);
				log.info("Schedule job '" + job.getSignature() + "' ok. Currently scheduling's size is " + countOfScheduling());
			}
		});
	}

	@Override
	public void unscheduleJob(Job job)throws JobException {
		if (hasScheduled(job)) {
			Future future = schedulingCache.remove(job);
			if (future != null) {
				future.cancel();
			}
			log.info("Unschedule job: " + job.getSignature());
		}
	}

	@Override
	public boolean hasScheduled(Job job) throws JobException {
		return hasJob(job) && schedulingCache.containsKey(job);
	}

	@Override
	public void runJob(Job job, Object attachment) {
		scheduler.runJob(job, attachment);
	}

	@Override
	public void pauseJob(Job job) throws JobException {
		if (hasScheduled(job)) {
			setJobState(job, JobState.PAUSED);
			if (log.isTraceEnabled()) {
				log.trace("Pause the job: " + job.getSignature());
			}
		}
	}

	@Override
	public void resumeJob(Job job) throws  JobException {
		if (hasScheduled(job)) {
			setJobState(job, JobState.RUNNING);
			if (log.isTraceEnabled()) {
				log.trace("Resume the job: " + job.getSignature());
			}
		}
	}

	@Override
	public void doSchedule() {
		trigger.notifyObservers();
		log.info("Run all jobs now.");
	}

	@Override
	public int countOfScheduling() {
		return schedulingCache.size();
	}

	@Override
	public Future getFuture(Job job) throws JobException {
		if (!schedulingCache.containsKey(job)) {
			throw new JobException("Not scheduling");
		}
		return schedulingCache.get(job);
	}

	@Override
	public void close() {
		for (Map.Entry<Job, Future> entry : schedulingCache.entrySet()) {
			entry.getValue().cancel();
		}
		schedulingCache.clear();
	}

	protected abstract void setJobState(Job job, JobState jobState) throws JobException;

}
